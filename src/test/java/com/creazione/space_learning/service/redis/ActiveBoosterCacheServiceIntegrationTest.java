package com.creazione.space_learning.service.redis;

import com.creazione.space_learning.entities.ActiveBooster;
import com.creazione.space_learning.enums.ResourceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ActiveBoosterCacheServiceIntegrationTest {

    @Autowired
    private ActiveBoosterCacheService activeBoosterCacheService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final Long USER_ID = 1L;
    private ActiveBooster booster1;
    private ActiveBooster booster2;
    private List<ActiveBooster> boosters;

    @BeforeEach
    void setUp() {
        // Очищаем Redis перед каждым тестом
        redisTemplate.getConnectionFactory().getConnection().flushAll();

        // Создаем тестовые активные бустеры
        Instant now = Instant.now();
        booster1 = new ActiveBooster();
        booster1.setName(ResourceType.ACCELERATION_METAL);
        booster1.setValue(0.5);
        booster1.setStartsAt(now);
        booster1.setEndsAt(now.plusSeconds(3600));

        booster2 = new ActiveBooster();
        booster2.setName(ResourceType.ACCELERATION_WOOD);
        booster2.setValue(0.3);
        booster2.setStartsAt(now);
        booster2.setEndsAt(now.plusSeconds(1800));

        boosters = List.of(booster1, booster2);
    }

    @Test
    void testCacheAndGetActiveBoosters() {
        // Сохраняем бустеры
        activeBoosterCacheService.cacheActiveBoosters(USER_ID, boosters);

        // Получаем бустеры из кэша
        List<ActiveBooster> cachedBoosters = activeBoosterCacheService.getActiveBoosters(USER_ID);

        // Проверяем, что бустеры сохранены и получены корректно
        assertNotNull(cachedBoosters);
        assertEquals(2, cachedBoosters.size());

        // Проверяем, что поля бустеров сохранились правильно
        ActiveBooster cachedBooster1 = cachedBoosters.get(0);
        assertEquals(ResourceType.ACCELERATION_METAL, cachedBooster1.getName());
        assertEquals(0.5, cachedBooster1.getValue());

        ActiveBooster cachedBooster2 = cachedBoosters.get(1);
        assertEquals(ResourceType.ACCELERATION_WOOD, cachedBooster2.getName());
        assertEquals(0.3, cachedBooster2.getValue());
    }

    @Test
    void testGetActiveBoostersByNameIn() {
        // Сохраняем бустеры
        activeBoosterCacheService.cacheActiveBoosters(USER_ID, boosters);

        // Получаем бустеры по типам
        List<ActiveBooster> metalBoosters = activeBoosterCacheService.getActiveBoostersByNameIn(
                USER_ID, List.of(ResourceType.ACCELERATION_METAL)
        );

        // Проверяем, что найден только один бустер этого типа
        assertEquals(1, metalBoosters.size());
        assertEquals(ResourceType.ACCELERATION_METAL, metalBoosters.get(0).getName());
    }

    @Test
    void testDeleteActiveBoosters() {
        // Сохраняем бустеры
        activeBoosterCacheService.cacheActiveBoosters(USER_ID, boosters);

        // Проверяем, что бустеры сохранены
        assertFalse(activeBoosterCacheService.getActiveBoosters(USER_ID).isEmpty());

        // Удаляем бустеры
        activeBoosterCacheService.deleteActiveBoosters(USER_ID);

        // Проверяем, что бустеры удалены
        assertTrue(activeBoosterCacheService.getActiveBoosters(USER_ID).isEmpty());
    }

    @Test
    void testMarkActiveBoostersAsEmpty() {
        // Помечаем список бустеров как пустой
        activeBoosterCacheService.markActiveBoostersAsEmpty(USER_ID);

        // Проверяем, что список помечен как пустой
        assertTrue(activeBoosterCacheService.isActiveBoostersEmpty(USER_ID));

        // Проверяем, что получение бустеров возвращает пустой список
        assertTrue(activeBoosterCacheService.getActiveBoosters(USER_ID).isEmpty());
    }
}