package com.creazione.space_learning.service.redis;

import com.creazione.space_learning.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.creazione.space_learning.service.redis.CacheKey.*;

/*
 userId = это telegram_id
 */

@Service
@RequiredArgsConstructor
public class UserCacheService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ResourceCacheService resourceCacheService;
    private final BuildingCacheService buildingCacheService;
    private final InventoryBoosterCacheService inventoryBoosterCacheService;
    private final IdTelegramCacheService idTelegramCacheService;

    public void cacheFullUser(UserDto user) {
        Long telegramId = user.getTelegramId();
        if (!user.getResources().isEmpty()) {
            resourceCacheService.cacheResources(telegramId, user.getResources());
        }
        if (!user.getBuildings().isEmpty()) {
            buildingCacheService.cacheBuildings(telegramId, user.getBuildings());
        }
        if (!user.getBoosters().isEmpty()) {
            inventoryBoosterCacheService.cacheInventoryBoosters(telegramId, user.getBoosters());
        } else {
            inventoryBoosterCacheService.markInventoryBoostersAsEmpty(telegramId);
        }
        cacheUser(user);
    }

    public void cacheUser(UserDto user) {
        Long telegramId = user.getTelegramId();
        deleteUser(telegramId);
        // Сохраняем пользователя без связанных данных
        user.setBoosters(new ArrayList<>());
        user.setBuildings(new ArrayList<>());
        user.setResources(new ArrayList<>());
        redisTemplate.opsForValue().set(USER_KEY_PREFIX.getName() + telegramId, user);
        redisTemplate.expire(USER_KEY_PREFIX.getName() + telegramId, 1, TimeUnit.HOURS);
        idTelegramCacheService.saveIdTelegramMapping(user.getId(), telegramId);
    }

    public boolean hasUser(Long telegramId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(USER_KEY_PREFIX.getName() + telegramId));
    }

    public UserDto getUser(Long telegramId) {
        return (UserDto) redisTemplate.opsForValue().get(USER_KEY_PREFIX.getName() + telegramId);
    }

    public void updateUser(UserDto userDto) {
        Long telegramId = userDto.getTelegramId();
        redisTemplate.opsForValue().set(USER_KEY_PREFIX.getName() + telegramId, userDto);
        idTelegramCacheService.updateTelegramId(userDto.getId(), telegramId);
        redisTemplate.expire(USER_KEY_PREFIX.getName() + telegramId, 1, TimeUnit.HOURS);
    }

    public void deleteFullUser(Long telegramId) {
        UserDto dto = (UserDto) redisTemplate.opsForValue().get(USER_KEY_PREFIX.getName() + telegramId);
        resourceCacheService.clearResourcesEmptyMark(telegramId);
        buildingCacheService.clearBuildingsEmptyMark(telegramId);
        inventoryBoosterCacheService.clearInventoryBoostersEmptyMark(telegramId);

        List<String> keysToDelete = new ArrayList<>(Arrays.asList(
                USER_KEY_PREFIX.getName() + telegramId,
                RESOURCE_KEY_PREFIX.getName() + telegramId,
                BUILDING_KEY_PREFIX.getName() + telegramId,
                INVENTORY_BOOSTERS_KEY.getName() + telegramId
        ));

        if (dto != null) {
            keysToDelete.add(ID_TELEGRAM_MAPPING.getKey(dto.getId()));
        }

        redisTemplate.delete(keysToDelete);


    }

    public void deleteUser(Long telegramId) {
        UserDto dto = (UserDto) redisTemplate.opsForValue().get(USER_KEY_PREFIX.getName() + telegramId);
        if (dto != null) {
            idTelegramCacheService.deleteMapping(dto.getId());
        }
        redisTemplate.delete(USER_KEY_PREFIX.getName() + telegramId);

    }
}