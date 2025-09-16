package com.creazione.space_learning.service.redis;

import com.creazione.space_learning.dto.UserDto;
import com.creazione.space_learning.entities.postgres.BuildingP;
import com.creazione.space_learning.entities.postgres.InventoryBoosterP;
import com.creazione.space_learning.entities.postgres.ResourceP;
import com.creazione.space_learning.enums.BuildingType;
import com.creazione.space_learning.enums.Emoji;
import com.creazione.space_learning.enums.ResourceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserCacheServiceIntegrationTest {

    @Autowired
    private UserCacheService userCacheService;

    @Autowired
    private ResourceCacheService resourceCacheService;

    @Autowired
    private BuildingCacheService buildingCacheService;

    @Autowired
    private InventoryBoosterCacheService inventoryBoosterCacheService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private UserDto testUser;

    @BeforeEach
    void setUp() {
        // Очищаем Redis перед каждым тестом
        redisTemplate.getConnectionFactory().getConnection().flushAll();

        // Создаем тестового пользователя
        testUser = new UserDto();
        testUser.setId(1L);
        testUser.setTelegramId(12345L);
        testUser.setName("Test User");

        // Создаем тестовые ресурсы
        ResourceP metal = new ResourceP(ResourceType.METAL, Emoji.ROCK, 100.0);
        ResourceP wood = new ResourceP(ResourceType.WOOD, Emoji.WOOD, 50.0);
        testUser.setResources(Arrays.asList(metal, wood));

        // Создаем тестовые здания
        BuildingP metalBuilding = new BuildingP(BuildingType.METAL_BUILDING, ResourceType.METAL);
        metalBuilding.setLevel(2);
        BuildingP woodBuilding = new BuildingP(BuildingType.WOOD_BUILDING, ResourceType.WOOD);
        woodBuilding.setLevel(1);
        testUser.setBuildings(Arrays.asList(metalBuilding, woodBuilding));

        // Создаем тестовые бустеры
        InventoryBoosterP booster1 = new InventoryBoosterP();
        booster1.setName(ResourceType.ACCELERATION_METAL);
        booster1.setQuantity(3.0);

        InventoryBoosterP booster2 = new InventoryBoosterP();
        booster2.setName(ResourceType.ACCELERATION_WOOD);
        booster2.setQuantity(2.0);
        testUser.setBoosters(Arrays.asList(booster1, booster2));
    }

    @Test
    void testCacheAndGetUser() {
        // Сохраняем пользователя
        userCacheService.cacheUser(testUser);

        // Получаем пользователя из кэша
        UserDto cachedUser = userCacheService.getUser(testUser.getTelegramId());

        // Проверяем, что пользователь сохранен и получен корректно
        assertNotNull(cachedUser);
        assertEquals(testUser.getId(), cachedUser.getId());
        assertEquals(testUser.getTelegramId(), cachedUser.getTelegramId());
        assertEquals(testUser.getName(), cachedUser.getName());

        // Проверяем, что связанные данные не сохраняются при cacheUser
        assertTrue(cachedUser.getResources().isEmpty());
        assertTrue(cachedUser.getBuildings().isEmpty());
        assertTrue(cachedUser.getBoosters().isEmpty());
    }

    @Test
    void testCacheAndGetFullUser() {
        // Сохраняем полного пользователя
        userCacheService.cacheFullUser(testUser);

        // Получаем пользователя из кэша
        UserDto cachedUser = userCacheService.getUser(testUser.getTelegramId());

        // Проверяем, что пользователь сохранен
        assertNotNull(cachedUser);
        assertEquals(testUser.getId(), cachedUser.getId());
        assertEquals(testUser.getTelegramId(), cachedUser.getTelegramId());
        assertEquals(testUser.getName(), cachedUser.getName());

        // Проверяем, что ресурсы сохранены и доступны отдельно
        List<ResourceP> cachedResources = resourceCacheService.getResources(testUser.getTelegramId());
        assertEquals(2, cachedResources.size());

        // Проверяем, что здания сохранены и доступны отдельно
        List<BuildingP> cachedBuildings = buildingCacheService.getBuildings(testUser.getTelegramId());
        assertEquals(2, cachedBuildings.size());

        // Проверяем, что бустеры сохранены и доступны отдельно
        List<InventoryBoosterP> cachedBoosters = inventoryBoosterCacheService.getInventoryBoosters(testUser.getTelegramId());
        assertEquals(2, cachedBoosters.size());
    }

    @Test
    void testUserExistenceCheck() {
        // Проверяем, что пользователя нет в кэше
        assertFalse(userCacheService.hasUser(testUser.getTelegramId()));

        // Сохраняем пользователя
        userCacheService.cacheUser(testUser);

        // Проверяем, что пользователь теперь есть в кэше
        assertTrue(userCacheService.hasUser(testUser.getTelegramId()));
    }

    @Test
    void testDeleteUser() {
        // Сохраняем пользователя
        userCacheService.cacheUser(testUser);
        assertTrue(userCacheService.hasUser(testUser.getTelegramId()));

        // Удаляем пользователя
        userCacheService.deleteUser(testUser.getTelegramId());

        // Проверяем, что пользователя больше нет в кэше
        assertFalse(userCacheService.hasUser(testUser.getTelegramId()));
        assertNull(userCacheService.getUser(testUser.getTelegramId()));
    }

    @Test
    void testDeleteFullUser() {
        // Сохраняем полного пользователя
        userCacheService.cacheFullUser(testUser);

        // Проверяем, что все данные сохранены
        assertTrue(userCacheService.hasUser(testUser.getTelegramId()));
        assertFalse(resourceCacheService.getResources(testUser.getTelegramId()).isEmpty());
        assertFalse(buildingCacheService.getBuildings(testUser.getTelegramId()).isEmpty());
        assertFalse(inventoryBoosterCacheService.getInventoryBoosters(testUser.getTelegramId()).isEmpty());

        // Удаляем все данные пользователя
        userCacheService.deleteFullUser(testUser.getTelegramId());

        // Проверяем, что все данные удалены
        assertFalse(userCacheService.hasUser(testUser.getTelegramId()));
        assertTrue(resourceCacheService.getResources(testUser.getTelegramId()).isEmpty());
        assertTrue(buildingCacheService.getBuildings(testUser.getTelegramId()).isEmpty());
        assertTrue(inventoryBoosterCacheService.getInventoryBoosters(testUser.getTelegramId()).isEmpty());
    }

    @Test
    void testUpdateUser() {
        // Сохраняем пользователя
        userCacheService.cacheUser(testUser);

        // Обновляем данные пользователя
        UserDto updatedUser = new UserDto();
        updatedUser.setId(1L);
        updatedUser.setTelegramId(12345L);
        updatedUser.setName("Updated User");
        userCacheService.updateUser(updatedUser);

        // Получаем обновленного пользователя
        UserDto cachedUser = userCacheService.getUser(testUser.getTelegramId());

        // Проверяем, что данные обновились
        assertNotNull(cachedUser);
        assertEquals("Updated User", cachedUser.getName());
    }
}