package com.creazione.space_learning.utils;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import lombok.extern.log4j.Log4j2;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Log4j2
@Deprecated
public class ThrottledMessageSender {
    public enum MessagePriority { HIGH, NORMAL }

    private final AbsSender botInstance;
    private final PriorityBlockingQueue<Answer> highPriorityQueue =
            new PriorityBlockingQueue<>(100, (a1, a2) -> -1); // Всегда в приоритете
    private final BlockingQueue<Answer> normalPriorityQueue = new LinkedBlockingQueue<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final ExecutorService sendExecutor = Executors.newVirtualThreadPerTaskExecutor();
    private final int maxMessagesPerSecond;
    private final int delayMs;
    private final AtomicInteger inProgress = new AtomicInteger(0);

    public ThrottledMessageSender(AbsSender botInstance, int maxMessagesPerSecond) {
        this.botInstance = botInstance;
        this.maxMessagesPerSecond = maxMessagesPerSecond;
        this.delayMs = 1000 / maxMessagesPerSecond;
        startScheduler();
    }

    private void startScheduler() {
        scheduler.scheduleAtFixedRate(() -> {
            if (inProgress.get() > 0) return; // Не мешаем активной отправке

            Answer answer = getNextMessage();
            if (answer != null) {
                inProgress.incrementAndGet();
                sendExecutor.submit(() -> {
                    try {
                        sendAllActions(answer);
                    } finally {
                        inProgress.decrementAndGet();
                    }
                });
            }
        }, 0, delayMs, TimeUnit.MILLISECONDS);
    }

    private Answer getNextMessage() {
        Answer answer = highPriorityQueue.poll();
        if (answer == null) {
            answer = normalPriorityQueue.poll();
        }
        return answer;
    }

    private void sendAllActions(Answer answer) {
        withTelegramApi(() -> {
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
                throw new RuntimeException(e);
            }
            return null;
        });
    }

    private void withTelegramApi(Runnable action) {
        try {
            action.run();
        } catch (Exception e) {
            handleApiException(e);
        }
    }

    private <T> T withTelegramApi(Supplier<T> action) {
        try {
            return action.get();
        } catch (Exception e) {
            handleApiException(e);
            return null;
        }
    }

    private void handleApiException(Exception e) {
        System.err.println("Telegram API error: " + e.getMessage());
        if (e instanceof TelegramApiException apiEx) {
            System.err.println("Error code: " + apiEx.getCause());
            System.err.println("Response: " + apiEx.getCause().getMessage());
        }
        e.printStackTrace();
    }

    public void enqueueMessage(Answer answer) {
        enqueueMessage(answer, MessagePriority.NORMAL);
    }

    public void enqueueMessage(Answer answer, MessagePriority priority) {
        // Очистка части нормальной очереди при перегрузке
        if (normalPriorityQueue.size() > 1000) {
            log.warn("Clearing normal queue due to overload");
            normalPriorityQueue.clear();
        }
        try {
            switch (priority) {
                case HIGH -> highPriorityQueue.put(answer);
                case NORMAL -> normalPriorityQueue.put(answer);
            }
        } catch (InterruptedException ie) {
            System.out.println("Error InterruptedException from class: " + ThrottledMessageSender.class.getName());
        }
    }

    public void enqueueTransaction(Answer transactionAnswer) {
        // Очистка части нормальной очереди при перегрузке
        if (normalPriorityQueue.size() > 1000) {
            log.warn("Clearing normal queue due to overload");
            normalPriorityQueue.clear();
        }

        enqueueMessage(transactionAnswer, MessagePriority.HIGH);

        // Можно добавить уведомление админу
        if (highPriorityQueue.size() > 50) {
            //sendAdminAlert("⚠️ High-priority queue overload!");
        }
    }

    public void shutdown() {
        scheduler.shutdown();
        sendExecutor.shutdown();
        try {
            if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
            if (!sendExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                sendExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}