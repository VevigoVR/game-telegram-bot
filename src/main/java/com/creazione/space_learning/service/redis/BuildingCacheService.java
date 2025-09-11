package com.creazione.space_learning.service.redis;

import com.creazione.space_learning.entities.Building;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.creazione.space_learning.service.redis.CacheKey.*;

@Service
@RequiredArgsConstructor
public class BuildingCacheService {
    private final RedisTemplate<String, Object> redisTemplate;

    public void cacheBuildings(Long userId, List<Building> buildings) {
        deleteBuildings(userId);
        String key = BUILDING_KEY_PREFIX.getName() + userId;

        if (buildings != null && !buildings.isEmpty()) {
            // Сохраняем весь список зданий как единый объект
            redisTemplate.opsForValue().set(key, buildings);
            redisTemplate.expire(key, 1, TimeUnit.HOURS);
        } else {
            markBuildingsAsEmpty(userId);
        }
    }


    public void cacheBuildingsHash(Long userId, List<Building> buildings) {
        deleteBuildings(userId);
        String hashKey = BUILDING_KEY_PREFIX.getName() + userId;

        if (buildings != null && !buildings.isEmpty()) {
            Map<String, Building> buildingMap = new HashMap<>();
            buildings.forEach(building ->
                    buildingMap.put(building.getName().name(), building)
            );

            redisTemplate.opsForHash().putAll(hashKey, buildingMap);
            redisTemplate.expire(hashKey, 1, TimeUnit.HOURS);
        } else {
            markBuildingsAsEmpty(userId);
        }
    }

    public boolean hasBuildings(Long userId) {
        return redisTemplate.hasKey(BUILDING_KEY_PREFIX.getName() + userId);
    }

    public boolean hasBuildingsHash(Long userId) {
        Long size = redisTemplate.opsForHash().size(BUILDING_KEY_PREFIX.getName() + userId);
        return size > 0;
    }

    public List<Building> getBuildings(Long userId) {
        String key = BUILDING_KEY_PREFIX.getName() + userId;

        // Проверяем, есть ли отметка о пустоте
        if (isBuildingsEmpty(userId)) {
            return List.of();
        }

        // Получаем весь список зданий
        Object buildings = redisTemplate.opsForValue().get(key);

        if (buildings instanceof List) {
            try {
                @SuppressWarnings("unchecked")
                List<Building> result = (List<Building>) buildings;
                return result;
            } catch (ClassCastException e) {
                // Если произошла ошибка приведения типа, очищаем кэш
                deleteBuildings(userId);
                return List.of();
            }
        }

        return List.of();
    }

    public List<Building> getBuildingsHash(Long userId) {
        List<Object> buildings = redisTemplate.opsForHash()
                .values(BUILDING_KEY_PREFIX.getName() + userId);
        return buildings.stream()
                .map(obj -> (Building) obj)
                .collect(Collectors.toList());
    }

    public void updateSingleBuilding(Long userId, Building building) {
        String key = BUILDING_KEY_PREFIX.getName() + userId;

        // Получаем текущий список зданий
        List<Building> buildings = getBuildings(userId);

        // Удаляем старое здание с таким же именем (если есть)
        buildings = buildings.stream()
                .filter(b -> !b.getName().equals(building.getName()))
                .collect(Collectors.toList());

        // Добавляем обновленное здание
        buildings.add(building);

        // Сохраняем обновленный список
        redisTemplate.opsForValue().set(key, buildings);
        redisTemplate.expire(key, 1, TimeUnit.HOURS);

        // Убираем отметку о пустоте
        clearBuildingsEmptyMark(userId);
    }

    public void updateSingleBuildingHash(Long userId, Building building) {
        clearBuildingsEmptyMark(userId);
        // Обновляем конкретное здание
        redisTemplate.opsForHash().put(
                BUILDING_KEY_PREFIX.getName() + userId,
                building.getName().name(),
                building
        );
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
