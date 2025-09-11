package com.creazione.space_learning.service.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.creazione.space_learning.service.redis.CacheKey.*;

@Service
@RequiredArgsConstructor
public class IdTelegramCacheService {
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Сохраняет отображение id → telegramId
     */
    public void saveIdTelegramMapping(Long id, Long telegramId) {
        String key = ID_TELEGRAM_MAPPING.getKey(id);
        redisTemplate.opsForValue().set(key, telegramId);
        redisTemplate.expire(ID_TELEGRAM_MAPPING.getKey(id), 61, TimeUnit.MINUTES);
    }

    /**
     * Находит telegramId по id
     */
    public Optional<Long> findTelegramIdById(Long id) {
        String key = ID_TELEGRAM_MAPPING.getKey(id);
        Object value = redisTemplate.opsForValue().get(key);

        if (value instanceof Long) {
            return Optional.of((Long) value);
        } else if (value instanceof Integer) {
            return Optional.of(((Integer) value).longValue());
        }

        return Optional.empty();
    }

    /**
     * Обновляет telegramId для заданного id
     */
    public void updateTelegramId(Long id, Long newTelegramId) {
        String key = ID_TELEGRAM_MAPPING.getKey(id);
        redisTemplate.opsForValue().set(key, newTelegramId);
        redisTemplate.expire(ID_TELEGRAM_MAPPING.getKey(id), 61, TimeUnit.MINUTES);
    }

    /**
     * Удаляет отображение для заданного id
     */
    public void deleteMapping(Long id) {
        String key = ID_TELEGRAM_MAPPING.getKey(id);
        redisTemplate.delete(key);
    }

    /**
     * Проверяет существование отображения для заданного id
     */
    public boolean exists(Long id) {
        String key = ID_TELEGRAM_MAPPING.getKey(id);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * Получает все отображения (для административных целей)
     * Внимание: этот метод может быть медленным при большом количестве ключей
     */
    public void getAllMappings() {
        // Для реализации этого метода нужно использовать шаблон ключей
        // Это более сложная операция, которую обычно не рекомендуется выполнять
        // в продакшн-среде с большим объемом данных
    }
}