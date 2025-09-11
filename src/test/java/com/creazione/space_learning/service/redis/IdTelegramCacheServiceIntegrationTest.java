package com.creazione.space_learning.service.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class IdTelegramCacheServiceIntegrationTest {

    @Autowired
    private IdTelegramCacheService idTelegramCacheService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final Long TEST_ID = 1L;
    private static final Long TEST_TELEGRAM_ID = 12345L;

    @BeforeEach
    void setUp() {
        // Очищаем Redis перед каждым тестом
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @Test
    void testSaveAndFindMapping() {
        // Сохраняем отображение
        idTelegramCacheService.saveIdTelegramMapping(TEST_ID, TEST_TELEGRAM_ID);

        // Ищем отображение
        Optional<Long> foundTelegramId = idTelegramCacheService.findTelegramIdById(TEST_ID);

        // Проверяем, что отображение найдено и корректно
        assertTrue(foundTelegramId.isPresent());
        assertEquals(TEST_TELEGRAM_ID, foundTelegramId.get());
    }

    @Test
    void testUpdateMapping() {
        // Сохраняем первоначальное отображение
        idTelegramCacheService.saveIdTelegramMapping(TEST_ID, TEST_TELEGRAM_ID);

        // Обновляем отображение
        Long newTelegramId = 54321L;
        idTelegramCacheService.updateTelegramId(TEST_ID, newTelegramId);

        // Проверяем, что отображение обновилось
        Optional<Long> foundTelegramId = idTelegramCacheService.findTelegramIdById(TEST_ID);
        assertTrue(foundTelegramId.isPresent());
        assertEquals(newTelegramId, foundTelegramId.get());
    }

    @Test
    void testDeleteMapping() {
        // Сохраняем отображение
        idTelegramCacheService.saveIdTelegramMapping(TEST_ID, TEST_TELEGRAM_ID);

        // Проверяем, что отображение существует
        assertTrue(idTelegramCacheService.exists(TEST_ID));

        // Удаляем отображение
        idTelegramCacheService.deleteMapping(TEST_ID);

        // Проверяем, что отображение удалено
        assertFalse(idTelegramCacheService.exists(TEST_ID));
        assertFalse(idTelegramCacheService.findTelegramIdById(TEST_ID).isPresent());
    }

    @Test
    void testExistsCheck() {
        // Проверяем, что отображение не существует
        assertFalse(idTelegramCacheService.exists(TEST_ID));

        // Сохраняем отображение
        idTelegramCacheService.saveIdTelegramMapping(TEST_ID, TEST_TELEGRAM_ID);

        // Проверяем, что отображение теперь существует
        assertTrue(idTelegramCacheService.exists(TEST_ID));
    }

    @Test
    void testFindNonExistentMapping() {
        // Пытаемся найти несуществующее отображение
        Optional<Long> foundTelegramId = idTelegramCacheService.findTelegramIdById(999L);

        // Проверяем, что отображение не найдено
        assertFalse(foundTelegramId.isPresent());
    }
}