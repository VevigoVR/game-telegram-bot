package com.creazione.space_learning.service.redis;

import com.creazione.space_learning.entities.game_entity.BuildingDto;
import com.creazione.space_learning.entities.redis.BuildingR;
import com.creazione.space_learning.game.buildings.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.creazione.space_learning.service.redis.CacheKey.*;

@Service
@RequiredArgsConstructor
public class BuildingCacheService {
    private final RedisTemplate<String, Object> redisTemplate;

    public List<BuildingR> toRedisObjectList(List<BuildingDto> buildingDtoList) {
        List<BuildingR> buildingRList = new ArrayList<>();
        for (BuildingDto building : buildingDtoList) {
            buildingRList.add(toRedisObject(building));
        }
        return buildingRList;
    }

    public List<BuildingDto> toGameObjectList(List<BuildingR> buildingRList) {
        List<BuildingDto> buildingDtoList = new ArrayList<>();
        for (BuildingR building : buildingRList) {
            buildingDtoList.add(toGameObject(building));
        }
        return buildingDtoList;
    }

    private BuildingR toRedisObject(BuildingDto buildingDto) {
        return new BuildingR(
                buildingDto.getId(),
                buildingDto.getUserId(),
                buildingDto.getName(),
                buildingDto.getProduction(),
                buildingDto.getEmojiProduction(),
                buildingDto.getIncrementPrice(),
                buildingDto.getResourcesInBuilding(),
                buildingDto.getIncrementMining(),
                buildingDto.getQuantityMining(),
                buildingDto.getLevel(),
                buildingDto.isVisible(),
                buildingDto.getTimeToUpdate(),
                buildingDto.getLastTimeUpgrade(),
                buildingDto.getLastUpdate()
        );
    }

    private BuildingDto toGameObject(BuildingR buildingR) {
        switch (buildingR.getName()) {
            case METAL_BUILDING -> {
                MetalBuilding metalBuilding = new MetalBuilding();
                convertNotFinalFieldToGameObject(metalBuilding, buildingR);
                return metalBuilding;
            }
            case GOLD_BUILDING -> {
                GoldBuilding goldBuilding = new GoldBuilding();
                convertNotFinalFieldToGameObject(goldBuilding, buildingR);
                return goldBuilding;
            }
            case STONE_BUILDING -> {
                StoneBuilding stoneBuilding = new StoneBuilding();
                convertNotFinalFieldToGameObject(stoneBuilding, buildingR);
                return stoneBuilding;
            }
            case WOOD_BUILDING -> {
                WoodBuilding woodBuilding = new WoodBuilding();
                convertNotFinalFieldToGameObject(woodBuilding, buildingR);
                return woodBuilding;
            }
            case STORAGE_BUILDING -> {
                StorageBuilding storageBuilding = new StorageBuilding();
                convertNotFinalFieldToGameObject(storageBuilding, buildingR);
                return storageBuilding;
            }
            default -> {
                UnknownBuilding unknownBuilding = new UnknownBuilding();
                convertNotFinalFieldToGameObject(unknownBuilding, buildingR);
                return unknownBuilding;
            }
        }
    }

    private void convertNotFinalFieldToGameObject(BuildingDto buildingDto, BuildingR buildingR) {
        buildingDto.setId(buildingR.getId());
        buildingDto.setUserId(buildingR.getUserId());
        buildingDto.setIncrementPrice(buildingR.getIncrementPrice());
        buildingDto.setResourcesInBuilding(buildingR.getResourcesInBuilding());
        buildingDto.setIncrementMining(buildingR.getIncrementMining());
        buildingDto.setQuantityMining(buildingR.getQuantityMining());
        buildingDto.setLevel(buildingR.getLevel());
        buildingDto.setVisible(buildingR.isVisible());
        buildingDto.setTimeToUpdate(buildingR.getTimeToUpdate());
        buildingDto.setLastTimeUpgrade(buildingR.getLastTimeUpgrade());
        buildingDto.setLastUpdate(buildingR.getLastUpdate());
    }


    public void cacheBuildings(Long telegramId, List<BuildingDto> buildingDtos) {
        deleteBuildings(telegramId);
        List<BuildingR> buildings = toRedisObjectList(buildingDtos);
        String key = BUILDING_KEY_PREFIX.getName() + telegramId;

        if (buildings != null && !buildings.isEmpty()) {
            // Сохраняем весь список зданий как единый объект
            redisTemplate.opsForValue().set(key, buildings);
            redisTemplate.expire(key, 1, TimeUnit.HOURS);
        } else {
            markBuildingsAsEmpty(telegramId);
        }
    }

    public boolean hasBuildings(Long telegramId) {
        return redisTemplate.hasKey(BUILDING_KEY_PREFIX.getName() + telegramId);
    }

    public List<BuildingDto> getBuildings(Long telegramId) {
        String key = BUILDING_KEY_PREFIX.getName() + telegramId;

        // Проверяем, есть ли отметка о пустоте
        if (isBuildingsEmpty(telegramId)) {
            return List.of();
        }

        // Получаем весь список зданий
        Object buildings = redisTemplate.opsForValue().get(key);

        if (buildings instanceof List) {
            try {
                @SuppressWarnings("unchecked")
                List<BuildingDto> result = toGameObjectList((List<BuildingR>) buildings);
                return result;
            } catch (ClassCastException e) {
                // Если произошла ошибка приведения типа, очищаем кэш
                deleteBuildings(telegramId);
                return List.of();
            }
        }

        return List.of();
    }

    public void deleteBuildings(Long userId) {
        clearBuildingsEmptyMark(userId);
        redisTemplate.delete(BUILDING_KEY_PREFIX.getName() + userId);
    }

    public void markBuildingsAsEmpty(Long userId) {
        redisTemplate.opsForValue().set(EMPTY_BUILDINGS_KEY.getName() + userId, "true", 5, TimeUnit.MINUTES);
    }

    public boolean isBuildingsEmpty(Long userId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(EMPTY_BUILDINGS_KEY.getName() + userId));
    }

    public void clearBuildingsEmptyMark(Long userId) {
        redisTemplate.delete(EMPTY_BUILDINGS_KEY.getName() + userId);
    }
}
