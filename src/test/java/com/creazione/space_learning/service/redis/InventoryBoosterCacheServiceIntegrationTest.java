package com.creazione.space_learning.service.redis;

import com.creazione.space_learning.dto.InventoryBoosterDto;
import com.creazione.space_learning.entities.InventoryBooster;
import com.creazione.space_learning.enums.ResourceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class InventoryBoosterCacheServiceIntegrationTest {

    @Autowired
    private InventoryBoosterCacheService inventoryBoosterCacheService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final Long USER_ID = 1L;
    private InventoryBooster booster1;
    private InventoryBooster booster2;
    private List<InventoryBooster> boosters;

    @BeforeEach
    void setUp() {
        // Очищаем Redis перед каждым тестом
        redisTemplate.getConnectionFactory().getConnection().flushAll();

        // Создаем тестовые бустеры
        booster1 = new InventoryBooster(ResourceType.ACCELERATION_ALL, 0.5, 3600000L, 10.0);
        booster2 = new InventoryBooster(ResourceType.ACCELERATION_METAL, 0.3, 1800000L, 5.0);
        boosters = List.of(booster1, booster2);
    }

    @Test
    void testGetInventoryBoosterByName() {
        // Сохраняем бустеры
        inventoryBoosterCacheService.cacheInventoryBoosters(USER_ID, boosters);

        // Получаем конкретный бустер по имени
        Optional<InventoryBooster> foundBooster = inventoryBoosterCacheService.getInventoryBooster(USER_ID, "ACCELERATION_ALL");

        // Проверяем, что бустер найден
        assertTrue(foundBooster.isPresent());
        assertEquals(ResourceType.ACCELERATION_ALL, foundBooster.get().getName());
        assertEquals(0.5, foundBooster.get().getValue());
    }

    @Test
    void testGetInventoryBoostersByType() {
        // Сохраняем бустеры
        inventoryBoosterCacheService.cacheInventoryBoosters(USER_ID, boosters);

        // Получаем бустеры по типу
        List<InventoryBooster> metalBoosters = inventoryBoosterCacheService.getInventoryBoostersByName(USER_ID, ResourceType.ACCELERATION_METAL);

        // Проверяем, что найден только один бустер этого типа
        assertEquals(1, metalBoosters.size());
        assertEquals(ResourceType.ACCELERATION_METAL, metalBoosters.get(0).getName());
    }

    @Test
    void testDeleteInventoryBoosters() {
        // Сохраняем бустеры
        inventoryBoosterCacheService.cacheInventoryBoosters(USER_ID, boosters);

        // Проверяем, что бустеры сохранены
        assertFalse(inventoryBoosterCacheService.getInventoryBoosters(USER_ID).isEmpty());

        // Удаляем бустеры
        inventoryBoosterCacheService.deleteInventoryBoosters(USER_ID);

        // Проверяем, что бустеры удалены
        assertTrue(inventoryBoosterCacheService.getInventoryBoosters(USER_ID).isEmpty());
    }

    @Test
    void testMarkInventoryBoostersAsEmpty() {
        // Помечаем список бустеров как пустой
        inventoryBoosterCacheService.markInventoryBoostersAsEmpty(USER_ID);

        // Проверяем, что список помечен как пустой
        assertTrue(inventoryBoosterCacheService.isInventoryBoostersEmpty(USER_ID));

        // Проверяем, что получение бустеров возвращает пустой список
        assertTrue(inventoryBoosterCacheService.getInventoryBoosters(USER_ID).isEmpty());
    }

    @Test
    void testClearInventoryBoostersEmptyMark() {
        // Помечаем список бустеров как пустой
        inventoryBoosterCacheService.markInventoryBoostersAsEmpty(USER_ID);
        assertTrue(inventoryBoosterCacheService.isInventoryBoostersEmpty(USER_ID));

        // Убираем отметку о пустоте
        inventoryBoosterCacheService.clearInventoryBoostersEmptyMark(USER_ID);

        // Проверяем, что отметка убрана
        assertFalse(inventoryBoosterCacheService.isInventoryBoostersEmpty(USER_ID));
    }

    @Test
    void testCacheEmptyInventoryBoosters() {
        // Сохраняем пустой список бустеров
        inventoryBoosterCacheService.cacheInventoryBoosters(USER_ID, List.of());

        // Проверяем, что список помечен как пустой
        assertTrue(inventoryBoosterCacheService.isInventoryBoostersEmpty(USER_ID));

        // Проверяем, что получение бустеров возвращает пустой список
        assertTrue(inventoryBoosterCacheService.getInventoryBoosters(USER_ID).isEmpty());
    }

    @Test
    void testCacheNullInventoryBoosters() {
        // Сохраняем null вместо списка бустеров
        inventoryBoosterCacheService.cacheInventoryBoosters(USER_ID, null);

        // Проверяем, что список помечен как пустой
        assertTrue(inventoryBoosterCacheService.isInventoryBoostersEmpty(USER_ID));

        // Проверяем, что получение бустеров возвращает пустой список
        assertTrue(inventoryBoosterCacheService.getInventoryBoosters(USER_ID).isEmpty());
    }

    @Test
    void testCacheAndGetInventoryBoosters() {
        // Сохраняем бустеры
        inventoryBoosterCacheService.cacheInventoryBoosters(USER_ID, boosters);

        // Получаем бустеры из кэша
        List<InventoryBooster> cachedBoosters = inventoryBoosterCacheService.getInventoryBoosters(USER_ID);

        // Проверяем, что бустеры сохранены и получены корректно
        assertNotNull(cachedBoosters);
        assertEquals(2, cachedBoosters.size());

        // Проверяем, что поля бустеров сохранились правильно
        InventoryBooster cachedBooster1 = cachedBoosters.get(0);
        assertEquals(ResourceType.ACCELERATION_ALL, cachedBooster1.getName());
        assertEquals(0.5, cachedBooster1.getValue());
        assertEquals(3600000L, cachedBooster1.getDurationMilli());
        assertEquals(10.0, cachedBooster1.getQuantity());

        InventoryBooster cachedBooster2 = cachedBoosters.get(1);
        assertEquals(ResourceType.ACCELERATION_METAL, cachedBooster2.getName());
        assertEquals(0.3, cachedBooster2.getValue());
        assertEquals(1800000L, cachedBooster2.getDurationMilli());
        assertEquals(5.0, cachedBooster2.getQuantity());
    }

    @Test
    void testUpdateSingleInventoryBooster() {
        // Сохраняем бустеры
        inventoryBoosterCacheService.cacheInventoryBoosters(USER_ID, boosters);

        // Создаем обновленный бустер
        InventoryBooster updatedBooster = new InventoryBooster(ResourceType.ACCELERATION_ALL, 0.5, 3600000L, 10.0);

        // Обновляем бустер
        inventoryBoosterCacheService.updateInventoryBooster(USER_ID, updatedBooster);

        // Получаем обновленные бустеры
        List<InventoryBooster> updatedBoosters = inventoryBoosterCacheService.getInventoryBoosters(USER_ID);

        // Проверяем, что бустер обновился
        assertEquals(2, updatedBoosters.size());

        // Ищем обновленный бустер
        Optional<InventoryBooster> foundUpdatedBooster = updatedBoosters.stream()
                .filter(b -> b.getName() == ResourceType.ACCELERATION_ALL)
                .findFirst();

        assertTrue(foundUpdatedBooster.isPresent());
        assertEquals(0.5, foundUpdatedBooster.get().getValue());
        assertEquals(10.0, foundUpdatedBooster.get().getQuantity());
    }
}