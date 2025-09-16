package com.creazione.space_learning.service.redis;

import com.creazione.space_learning.entities.redis.InventoryBoosterR;
import com.creazione.space_learning.entities.postgres.InventoryBoosterP;
import com.creazione.space_learning.enums.ResourceType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.creazione.space_learning.service.redis.CacheKey.*;

@Service
@RequiredArgsConstructor
public class InventoryBoosterCacheService {
    private final RedisTemplate<String, Object> redisTemplate;

    // Методы преобразования
    private InventoryBoosterR toRedisObject(InventoryBoosterP booster) {
        return new InventoryBoosterR(
                booster.getId(),
                booster.getUserId(),
                booster.getName(),
                booster.getValue(),
                booster.getDurationMilli(),
                booster.getQuantity()
        );
    }

    private InventoryBoosterP toGameObject(InventoryBoosterR dto) {
        InventoryBoosterP booster = new InventoryBoosterP();
        booster.setId(dto.getId());
        booster.setUserId(dto.getUserId());
        booster.setName(dto.getName());
        booster.setValue(dto.getValue());
        booster.setDurationMilli(dto.getDurationMilli());
        booster.setQuantity(dto.getQuantity());
        return booster;
    }

    private List<InventoryBoosterR> toRedisObjectList(List<InventoryBoosterP> boosters) {
        return boosters.stream()
                .map(this::toRedisObject)
                .collect(Collectors.toList());
    }

    private List<InventoryBoosterP> toGameObjectList(List<InventoryBoosterR> boosterDtos) {
        return boosterDtos.stream()
                .map(this::toGameObject)
                .collect(Collectors.toList());
    }

    public void cacheInventoryBoosters(Long telegramId, List<InventoryBoosterP> boosters) {
            deleteInventoryBoosters(telegramId);
        if (boosters != null && !boosters.isEmpty()) {
            String key = INVENTORY_BOOSTERS_KEY.getKey(telegramId);
            List<InventoryBoosterR> boosterDtos = toRedisObjectList(boosters);
            redisTemplate.opsForValue().set(key, boosterDtos);
            redisTemplate.expire(key, 1, TimeUnit.HOURS);
        }
    }

    public Optional<InventoryBoosterP> getInventoryBooster(Long telegramId, String boosterName) {
        List<InventoryBoosterP> allBoosters = getInventoryBoosters(telegramId);
        return allBoosters.stream()
                .filter(booster -> booster.getName().name().equals(boosterName))
                .findFirst();
    }

    public List<InventoryBoosterP> getInventoryBoosters(Long telegramId) {
        String key = INVENTORY_BOOSTERS_KEY.getName() + telegramId;

        if (isInventoryBoostersEmpty(telegramId)) {
            return new ArrayList<>();
        }

        Object boosters = redisTemplate.opsForValue().get(key);

        if (boosters instanceof List) {
            try {
                @SuppressWarnings("unchecked")
                List<InventoryBoosterR> boosterDtos = (List<InventoryBoosterR>) boosters;
                return toGameObjectList(boosterDtos);
            } catch (ClassCastException e) {
                deleteInventoryBoosters(telegramId);
                return new ArrayList<>();
            }
        }

        return new ArrayList<>();
    }

    public List<InventoryBoosterP> getInventoryBoostersByName(Long telegramId, ResourceType type) {
        List<InventoryBoosterP> allBoosters = getInventoryBoosters(telegramId);
        return allBoosters.stream()
                .filter(booster -> booster.getName().equals(type))
                .collect(Collectors.toList());
    }

    public void addInventoryBooster(Long telegramId, InventoryBoosterP booster) {
        String key = INVENTORY_BOOSTERS_KEY.getName() + telegramId;

        // Получаем текущий список бустеров
        List<InventoryBoosterP> boosters = getInventoryBoosters(telegramId);

        // Добавляем новый бустер
        boosters.add(booster);

        // Сохраняем обновленный список
        List<InventoryBoosterR> boosterDtos = toRedisObjectList(boosters);
        redisTemplate.opsForValue().set(key, boosterDtos);
        redisTemplate.expire(key, 1, TimeUnit.HOURS);

        // Убираем отметку о пустоте
        clearInventoryBoostersEmptyMark(telegramId);
    }

    public void updateInventoryBooster(Long telegramId, InventoryBoosterP booster) {
        String key = INVENTORY_BOOSTERS_KEY.getName() + telegramId;

        // Получаем текущий список бустеров
        List<InventoryBoosterP> boosters = getInventoryBoosters(telegramId);

        if (boosters == null || boosters.isEmpty()) { return; }
        // Удаляем старый бустер с таким же именем и характеристиками
        boosters = boosters.stream()
                .filter(b -> !(b.getName().equals(booster.getName()) &&
                        b.getValue().equals(booster.getValue()) &&
                        b.getDurationMilli().equals(booster.getDurationMilli())))
                .collect(Collectors.toList());

        // Добавляем обновленный бустер
        boosters.add(booster);

        // Сохраняем обновленный список
        List<InventoryBoosterR> boosterDtos = toRedisObjectList(boosters);
        redisTemplate.opsForValue().set(key, boosterDtos);
        redisTemplate.expire(key, 1, TimeUnit.HOURS);

        // Убираем отметку о пустоте
        clearInventoryBoostersEmptyMark(telegramId);
    }

    public void removeInventoryBooster(Long telegramId, InventoryBoosterP booster) {
        String key = INVENTORY_BOOSTERS_KEY.getName() + telegramId;

        // Получаем текущий список бустеров
        List<InventoryBoosterP> boosters = getInventoryBoosters(telegramId);

        // Удаляем бустер с такими же характеристиками
        boosters = boosters.stream()
                .filter(b -> !(b.getName().equals(booster.getName()) &&
                        b.getValue().equals(booster.getValue()) &&
                        b.getDurationMilli().equals(booster.getDurationMilli())))
                .collect(Collectors.toList());

        // Сохраняем обновленный список
        if (boosters.isEmpty()) {
            markInventoryBoostersAsEmpty(telegramId);
        } else {
            List<InventoryBoosterR> boosterDtos = toRedisObjectList(boosters);
            redisTemplate.opsForValue().set(key, boosterDtos);
            redisTemplate.expire(key, 1, TimeUnit.HOURS);
        }
    }

    public boolean hasInventoryBooster(Long telegramId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(INVENTORY_BOOSTERS_KEY.getKey(telegramId)));
    }

    public void deleteInventoryBoosters(Long telegramId) {
        clearInventoryBoostersEmptyMark(telegramId);
        redisTemplate.delete(INVENTORY_BOOSTERS_KEY.getName() + telegramId);
    }

    public void markInventoryBoostersAsEmpty(Long telegramId) {
        redisTemplate.opsForValue().set(EMPTY_INVENTORY_BOOSTERS_KEY.getName() + telegramId, "true", 5, TimeUnit.MINUTES);
    }

    public boolean isInventoryBoostersEmpty(Long telegramId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(EMPTY_INVENTORY_BOOSTERS_KEY.getName() + telegramId));
    }

    public void clearInventoryBoostersEmptyMark(Long telegramId) {
        redisTemplate.delete(EMPTY_INVENTORY_BOOSTERS_KEY.getName() + telegramId);
    }
}