package com.creazione.space_learning.utils;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.redisson.api.*;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Deprecated
public class ThrottledMessageSender3 {
    public enum MessagePriority { HIGH, NORMAL }

    private final AbsSender botInstance;
    private final RedissonClient redisson;
    private final int maxActionsPerSecond;
    private final AtomicInteger inProgress = new AtomicInteger(0);
    private final ExecutorService sendExecutor = Executors.newVirtualThreadPerTaskExecutor();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    // Redis keys
    private final String actionCounterKey = "throttler:action:counter";
    private final String highPriorityQueueKey = "throttler:queue:high";
    private final String normalPriorityQueueKey = "throttler:queue:normal";
    private final String deadLetterQueueKey = "throttler:queue:dead";

    public ThrottledMessageSender3 (
            AbsSender botInstance,
            RedissonClient redisson,
            int maxActionsPerSecond
    ) {
        this.botInstance = botInstance;
        this.redisson = redisson;
        this.maxActionsPerSecond = maxActionsPerSecond;
        initRedisStructures();
        startScheduler();
    }

    private void initRedisStructures() {
        // Инициализация счётчика с автоматическим сбросом
        RAtomicLong counter = redisson.getAtomicLong(actionCounterKey);
        counter.set(0);

        // Установка TTL через 1 секунду (не deprecated метод)
        counter.expire(1, TimeUnit.SECONDS);

        // Создаём очереди, если не существуют
        if (!redisson.getBlockingQueue(highPriorityQueueKey).isExists()) {
            redisson.getBlockingQueue(highPriorityQueueKey).clear();
        }
        if (!redisson.getBlockingQueue(normalPriorityQueueKey).isExists()) {
            redisson.getBlockingQueue(normalPriorityQueueKey).clear();
        }
    }

    private void startScheduler() {
        // Сброс счетчика каждую секунду
        scheduler.scheduleAtFixedRate(() -> {
            RAtomicLong counter = redisson.getAtomicLong(actionCounterKey);
            counter.set(0);
            counter.expire(1, TimeUnit.SECONDS); // Обновляем TTL
        }, 0, 1, TimeUnit.SECONDS);

        // Основной обработчик
        scheduler.scheduleAtFixedRate(() -> {
            if (inProgress.get() > 0) return;

            try {
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
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
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
        if (answer.getSendPhoto() != null) count++;
        if (answer.getAnswerCallbackQuery() != null) count++;
        if (answer.getEditMessageReplyMarkup() != null) count++;
        if (answer.getNewTxt() != null) count++;
        if (answer.getDeleteMessage() != null) count++;
        if (answer.getEditMessageCaption() != null) count++;
        return count;
    }

    private Answer getNextMessage() throws InterruptedException {
        // Сначала проверяем очередь HIGH
        RBlockingQueue<Answer> highQueue = redisson.getBlockingQueue(highPriorityQueueKey);
        Answer answer = highQueue.poll();

        // Если нет HIGH, проверяем NORMAL
        if (answer == null) {
            RBlockingQueue<Answer> normalQueue = redisson.getBlockingQueue(normalPriorityQueueKey);
            answer = normalQueue.poll();
        }

        return answer;
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
            handleApiException(e, answer);
        }
    }

    private void handleApiException(TelegramApiException e, Answer answer) {
        // Определяем тип ошибки по сообщению
        boolean isRateLimit = e.getMessage() != null &&
                e.getMessage().contains("Too Many Requests");

        log.error("Telegram API error: {}", e.getMessage());

        if (isRateLimit) {
            handleRateLimitError();
        } else {
            // Для других ошибок сохраняем сообщение в dead letter queue
            saveToDeadLetterQueue(answer);
        }
    }

    private void handleRateLimitError() {
        try {
            int sleepTime = 1000; // Стандартная задержка
            log.warn("Rate limit hit! Sleeping for {} ms", sleepTime);
            Thread.sleep(sleepTime);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private void saveToDeadLetterQueue(Answer answer) {
        try {
            RBlockingQueue<Answer> deadQueue = redisson.getBlockingQueue(deadLetterQueueKey);
            deadQueue.put(answer);
            log.error("Message saved to dead-letter queue");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void enqueueMessage(Answer answer) {
        enqueueMessage(answer, MessagePriority.NORMAL);
    }

    public void enqueueMessage(Answer answer, MessagePriority priority) {
        try {
            RBlockingQueue<Answer> queue = priority == MessagePriority.HIGH ?
                    redisson.getBlockingQueue(highPriorityQueueKey) :
                    redisson.getBlockingQueue(normalPriorityQueueKey);

            // Автоматическая очистка normal-очереди при переполнении
            if (priority == MessagePriority.NORMAL && queue.size() > 1000) {
                log.warn("Clearing normal queue due to overload");
                queue.clear();
            }

            queue.put(answer);
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

        // Сохраняем метрики перед завершением
        log.info("Shutdown completed. Final stats: {}", getStats());
    }

    // Методы для мониторинга
    public String getStats() {
        return String.format(
                "HighQueue: %d, NormalQueue: %d, DeadQueue: %d, ActionCount: %d/%d",
                redisson.getBlockingQueue(highPriorityQueueKey).size(),
                redisson.getBlockingQueue(normalPriorityQueueKey).size(),
                redisson.getBlockingQueue(deadLetterQueueKey).size(),
                redisson.getAtomicLong(actionCounterKey).get(),
                maxActionsPerSecond
        );
    }

    public void processDeadLetterQueue() {
        RBlockingQueue<Answer> deadQueue = redisson.getBlockingQueue(deadLetterQueueKey);
        while (!deadQueue.isEmpty()) {
            try {
                Answer message = deadQueue.take();
                enqueueMessage(message, MessagePriority.HIGH);
                log.info("Retrying message from dead-letter queue");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}