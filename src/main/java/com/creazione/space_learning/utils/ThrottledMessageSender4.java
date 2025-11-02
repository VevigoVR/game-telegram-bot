package com.creazione.space_learning.utils;

import java.util.Arrays;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.redisson.api.*;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ThrottledMessageSender4 {
    public enum MessagePriority { HIGH, NORMAL }

    private final AbsSender botInstance;
    private final RedissonClient redisson;
    private final int maxActionsPerSecond;
    private final AtomicInteger inProgress = new AtomicInteger(0);
    private final ExecutorService sendExecutor = Executors.newVirtualThreadPerTaskExecutor();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    // In-memory очереди (без Redis)
    private final BlockingQueue<Answer> highPriorityQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<Answer> normalPriorityQueue = new LinkedBlockingQueue<>();

    // Redis keys только для счетчика
    private final String actionCounterKey = "throttler:action:counter";

    public ThrottledMessageSender4 (
            AbsSender botInstance,
            RedissonClient redisson,
            int maxActionsPerSecond
    ) {
        this.botInstance = botInstance;
        this.redisson = redisson;
        this.maxActionsPerSecond = maxActionsPerSecond;
        initRedisCounter();
        startScheduler();
    }

    private void initRedisCounter() {
        RAtomicLong counter = redisson.getAtomicLong(actionCounterKey);
        counter.set(0);
        counter.expire(1, TimeUnit.SECONDS);
    }

    private void startScheduler() {
        // Сброс счетчика каждую секунду
        scheduler.scheduleAtFixedRate(() -> {
            RAtomicLong counter = redisson.getAtomicLong(actionCounterKey);
            counter.set(0);
            counter.expire(1, TimeUnit.SECONDS);
        }, 0, 1, TimeUnit.SECONDS);

        // Основной обработчик
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
        }, 0, 10, TimeUnit.MILLISECONDS);
    }

    private boolean canSendActions(Answer answer) {
        RAtomicLong counter = redisson.getAtomicLong(actionCounterKey);
        long currentCount = counter.get();
        int required = countActions(answer);

        return (currentCount + required) <= maxActionsPerSecond;
    }

    private int countActions(Answer answer) {
        int count = 0;
        if (answer.getSendMessage() != null) count++;
        if (answer.getAnswerCallbackQuery() != null) count++;
        if (answer.getEditMessageReplyMarkup() != null) count++;
        if (answer.getNewTxt() != null) count++;
        if (answer.getDeleteMessage() != null) count++;
        if (answer.getEditMessageCaption() != null) count++;

        // Изображения считаем, но не храним в Redis
        if (answer.getSendPhoto() != null) count++;

        return count;
    }

    private Answer getNextMessage() {
        Answer answer = highPriorityQueue.poll();
        return answer != null ? answer : normalPriorityQueue.poll();
    }

    private void sendAllActions(Answer answer) {
        try {
            // Обновляем счётчик действий
            RAtomicLong counter = redisson.getAtomicLong(actionCounterKey);
            counter.addAndGet(countActions(answer));

            // Выполняем все действия
            if (answer.getSendMessage() != null) {
                botInstance.execute(answer.getSendMessage());
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
            if (answer.getSendPhoto() != null) {
                botInstance.execute(answer.getSendPhoto());
            }
        } catch (TelegramApiException e) {
            handleApiException(e);
        }
    }

    private void handleApiException(TelegramApiException e) {
        boolean isRateLimit = e.getMessage() != null &&
                e.getMessage().contains("Too Many Requests");

        log.error("Telegram API error: {}", e.getMessage());
        System.out.println(Arrays.toString(e.getStackTrace()));

        if (isRateLimit) {
            handleRateLimitError();
        }
    }

    private void handleRateLimitError() {
        try {
            log.warn("Rate limit hit! Sleeping for 1000 ms");
            Thread.sleep(1000);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
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
        log.info("Shutdown completed");
    }

    // Методы для мониторинга
    public String getStats() {
        return String.format(
                "HighQueue: %d, NormalQueue: %d, ActionCount: %d/%d",
                highPriorityQueue.size(),
                normalPriorityQueue.size(),
                redisson.getAtomicLong(actionCounterKey).get(),
                maxActionsPerSecond
        );
    }
}