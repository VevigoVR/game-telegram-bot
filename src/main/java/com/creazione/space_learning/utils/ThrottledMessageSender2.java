package com.creazione.space_learning.utils;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Deprecated
public class ThrottledMessageSender2 {
    public enum MessagePriority { HIGH, NORMAL }

    private final AbsSender botInstance;
    private final RedissonClient redissonClient;
    private final PriorityBlockingQueue<Answer> highPriorityQueue =
            new PriorityBlockingQueue<>(100, (a1, a2) -> -1);
    private final BlockingQueue<Answer> normalPriorityQueue = new LinkedBlockingQueue<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final ExecutorService sendExecutor = Executors.newVirtualThreadPerTaskExecutor();
    private final int maxActionsPerSecond;
    private final AtomicInteger inProgress = new AtomicInteger(0);
    private final String redisCounterKey = "telegram:action:counter";

    public ThrottledMessageSender2 (
            AbsSender botInstance,
            RedissonClient redissonClient,
            int maxActionsPerSecond
    ) {
        this.botInstance = botInstance;
        this.redissonClient = redissonClient;
        this.maxActionsPerSecond = maxActionsPerSecond;
        startScheduler();
        initRedisCounter();
    }

    private void initRedisCounter() {
        RAtomicLong counter = redissonClient.getAtomicLong(redisCounterKey);
        counter.expire(1, TimeUnit.SECONDS); // Автосброс каждую секунду
    }

    private void startScheduler() {
        // Сброс счетчика Redis каждую секунду
        scheduler.scheduleAtFixedRate(() -> {
            RAtomicLong counter = redissonClient.getAtomicLong(redisCounterKey);
            counter.set(0);
        }, 0, 1, TimeUnit.SECONDS);

        // Основной обработчик очереди
        scheduler.scheduleAtFixedRate(() -> {
            if (inProgress.get() > 0) return;

            Answer answer = getNextMessage();
            if (answer != null && canSendActions(answer)) {
                inProgress.incrementAndGet();
                sendExecutor.submit(() -> {
                    try {
                        sendAllActions(answer);
                    } finally {
                        inProgress.decrementAndGet();
                    }
                });
            }
        }, 0, 10, TimeUnit.MILLISECONDS); // Проверка каждые 10 мс
    }

    private boolean canSendActions(Answer answer) {
        RAtomicLong counter = redissonClient.getAtomicLong(redisCounterKey);
        long currentCount = counter.get();
        int required = countActions(answer);

        if (currentCount + required <= maxActionsPerSecond) {
            counter.addAndGet(required);
            return true;
        }
        return false;
    }

    private int countActions(Answer answer) {
        int count = 0;
        if (answer.getSendMessage() != null) count++;
        if (answer.getSendPhoto() != null) count++;
        if (answer.getAnswerCallbackQuery() != null) count++;
        if (answer.getEditMessageReplyMarkup() != null) count++;
        if (answer.getNewTxt() != null) count++;
        if (answer.getDeleteMessage() != null) count++;
        if (answer.getEditMessageCaption() != null) count++;
        return count;
    }

    private Answer getNextMessage() {
        Answer answer = highPriorityQueue.poll();
        return answer != null ? answer : normalPriorityQueue.poll();
    }

    private void sendAllActions(Answer answer) {
        try {
            if (answer.getSendMessage() != null) {
                botInstance.execute(answer.getSendMessage());
            }
            if (answer.getSendPhoto() != null) {
                botInstance.execute(answer.getSendPhoto());
            }
            if (answer.getAnswerCallbackQuery() != null) {
                botInstance.execute(answer.getAnswerCallbackQuery());
            }
            if (answer.getEditMessageReplyMarkup() != null) {
                botInstance.execute(answer.getEditMessageReplyMarkup());
            }
            if (answer.getNewTxt() != null) {
                botInstance.execute(answer.getNewTxt());
            }
            if (answer.getDeleteMessage() != null) {
                botInstance.execute(answer.getDeleteMessage());
            }
            if (answer.getEditMessageCaption() != null) {
                botInstance.execute(answer.getEditMessageCaption());
            }
        } catch (TelegramApiException e) {
            handleApiException(e);
        }
    }

    private void handleApiException(TelegramApiException e) {
        log.error("Telegram API error: {}", e.getMessage());
        if (e.getCause() != null) {
            log.error("Root cause: {}", e.getCause().getMessage());
        }

        // Автоматический бэкофф при 429 ошибке
        if (e.hashCode() == 429) {
            log.warn("Rate limit hit! Applying backoff...");
            try {
                Thread.sleep(1000); // Пауза 1 секунда
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void enqueueMessage(Answer answer) {
        enqueueMessage(answer, MessagePriority.NORMAL);
    }

    public void enqueueMessage(Answer answer, MessagePriority priority) {
        try {
            if (priority == MessagePriority.HIGH) {
                highPriorityQueue.put(answer);
            } else {
                if (normalPriorityQueue.size() > 1000) {
                    log.warn("Clearing normal queue due to overload");
                    normalPriorityQueue.clear();
                }
                normalPriorityQueue.put(answer);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Queue interrupted");
        }
    }

    public void enqueueTransaction(Answer transactionAnswer) {
        enqueueMessage(transactionAnswer, MessagePriority.HIGH);

        // Мониторинг переполнения high-очереди
        if (highPriorityQueue.size() > 100) {
            log.error("⚠️ CRITICAL: High-priority queue overload!");
        }
    }

    public void shutdown() {
        scheduler.shutdown();
        sendExecutor.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
            if (!sendExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                sendExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Методы для мониторинга
    public int getHighPriorityQueueSize() {
        return highPriorityQueue.size();
    }

    public int getNormalPriorityQueueSize() {
        return normalPriorityQueue.size();
    }

    public long getCurrentActionCount() {
        return redissonClient.getAtomicLong(redisCounterKey).get();
    }
}
