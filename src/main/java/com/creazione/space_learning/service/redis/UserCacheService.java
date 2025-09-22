package com.creazione.space_learning.service.redis;

import com.creazione.space_learning.entities.game_entity.UserDto;
import com.creazione.space_learning.entities.redis.UserR;
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

    public UserDto toGameObject(UserR userR) {

        return new UserDto(userR.getId(),
                userR.getTelegramId(),
                userR.getName(),
                userR.getBuildings() == null ? new ArrayList<>() : userR.getBuildings(),
                userR.getResources() == null ? new ArrayList<>() : resourceCacheService.toGameObjectList(userR.getResources()),
                userR.getBoosters() == null ? new ArrayList<>() : userR.getBoosters(),
                userR.getPlayerScore(),
                userR.getReferrer(),
                userR.getTotalReferrals(),
                userR.getNotices() == null ? new ArrayList<>() : userR.getNotices(),
                userR.isSuperAggregate(),
                userR.isPost(),
                userR.getUpdatedAt(),
                userR.getCreatedAt()
        );
    }

    public UserR toRedisObject(UserDto userDto) {
        return new UserR(userDto.getId(),
                userDto.getTelegramId(),
                userDto.getName(),
                userDto.getBuildings() == null ? new ArrayList<>() : userDto.getBuildings(),
                userDto.getResources() == null ? new ArrayList<>() : resourceCacheService.toRedisObjectList(userDto.getResources()),
                userDto.getBoosters() == null ? new ArrayList<>() : userDto.getBoosters(),
                userDto.getPlayerScore(),
                userDto.getReferrer(),
                userDto.getTotalReferrals(),
                userDto.getNotices() == null ? new ArrayList<>() : userDto.getNotices(),
                userDto.isSuperAggregate(),
                userDto.isPost(),
                userDto.getUpdatedAt(),
                userDto.getCreatedAt()
        );
    }

    public void cacheFullUser(UserDto userDto) {
        UserR user = toRedisObject(userDto);
        Long telegramId = user.getTelegramId();
        if (!user.getResources().isEmpty()) {
            resourceCacheService.cacheResources(telegramId, userDto.getResources());
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

    public void cacheUser(UserDto userDto) {
        UserR user = toRedisObject(userDto);
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

    private void cacheUser(UserR user) {
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
        UserR userR = (UserR) redisTemplate.opsForValue().get(USER_KEY_PREFIX.getName() + telegramId);
        if (userR != null) {
            return toGameObject(userR);
        } else {
            return null;
        }
    }

    public void updateUser(UserDto user) {
        UserR userRedis = toRedisObject(user);
        Long telegramId = userRedis.getTelegramId();
        redisTemplate.opsForValue().set(USER_KEY_PREFIX.getName() + telegramId, userRedis);
        idTelegramCacheService.updateTelegramId(userRedis.getId(), telegramId);
        redisTemplate.expire(USER_KEY_PREFIX.getName() + telegramId, 1, TimeUnit.HOURS);
    }

    public void deleteFullUser(Long telegramId) {
        UserR userR = (UserR) redisTemplate.opsForValue().get(USER_KEY_PREFIX.getName() + telegramId);
        resourceCacheService.clearResourcesEmptyMark(telegramId);
        buildingCacheService.clearBuildingsEmptyMark(telegramId);
        inventoryBoosterCacheService.clearInventoryBoostersEmptyMark(telegramId);

        List<String> keysToDelete = new ArrayList<>(Arrays.asList(
                USER_KEY_PREFIX.getName() + telegramId,
                RESOURCE_KEY_PREFIX.getName() + telegramId,
                BUILDING_KEY_PREFIX.getName() + telegramId,
                INVENTORY_BOOSTERS_KEY.getName() + telegramId
        ));

        if (userR != null) {
            keysToDelete.add(ID_TELEGRAM_MAPPING.getKey(userR.getId()));
        }

        redisTemplate.delete(keysToDelete);


    }

    public void deleteUser(Long telegramId) {
        UserR userR = (UserR) redisTemplate.opsForValue().get(USER_KEY_PREFIX.getName() + telegramId);
        if (userR != null) {
            idTelegramCacheService.deleteMapping(userR.getId());
        }
        redisTemplate.delete(USER_KEY_PREFIX.getName() + telegramId);

    }
}