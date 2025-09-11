package com.creazione.space_learning.service;

import com.creazione.space_learning.entities.AITrainingDataEntity;
import com.creazione.space_learning.repository.AITrainingDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class AIDataCollector {
    private final AITrainingDataRepository repository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Autowired
    public AIDataCollector(AITrainingDataRepository repository) {
        this.repository = repository;
    }

    public void logInteraction(Long userId, String message, String context) {
        // Пропускаем короткие и длинные сообщения
        if (message.length() < 3) return;
        if (message.length() > 1000) return;
        // Пропускаем частые сообщения от одного пользователя (>3 в минуту)
        if (isHighFrequency(userId)) return;

        // Асинхронное сохранение чтобы не блокировать основной поток
        executor.submit(() -> {
            try {
                AITrainingDataEntity data = new AITrainingDataEntity();
                data.setUserId(userId);
                data.setUserMessage(message);
                data.setContext(context);
                data.setTimestamp(OffsetDateTime.now());

                repository.save(data);
            } catch (Exception e) {
                System.out.println("Ошибка в сохранении данных ИИ");
                // Логирование ошибки без прерывания работы
            }
        });
    }

    private boolean isHighFrequency(Long userId) {
        // Простая проверка частоты
        long countLastMinute = repository.countByUserIdAndTimestampAfter(
                userId,
                OffsetDateTime.now().minusMinutes(1)
        );
        return countLastMinute > 3;
    }
}