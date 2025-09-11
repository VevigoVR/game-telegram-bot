package com.creazione.space_learning.service.redis;

import com.creazione.space_learning.dto.InventoryBoosterDto;
import com.creazione.space_learning.entities.InventoryBooster;
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
    private InventoryBoosterDto toDto(InventoryBooster booster) {
        return new InventoryBoosterDto(
                booster.getId(),
                booster.getUserId(),
                booster.getName(),
                booster.getValue(),
                booster.getDurationMilli(),
                booster.getQuantity()
        );
    }

    private InventoryBooster toEntity(InventoryBoosterDto dto) {
        InventoryBooster booster = new InventoryBooster();
        booster.setId(dto.getId());
        booster.setUserId(dto.getUserId());
        booster.setName(dto.getName());
        booster.setValue(dto.getValue());
        booster.setDurationMilli(dto.getDurationMilli());
        booster.setQuantity(dto.getQuantity());
        return booster;
    }

    private List<InventoryBoosterDto> toDtoList(List<InventoryBooster> boosters) {
        return boosters.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private List<InventoryBooster> toEntityList(List<InventoryBoosterDto> boosterDtos) {
        return boosterDtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    public void cacheInventoryBoosters(Long telegramId, List<InventoryBooster> boosters) {
        deleteInventoryBoosters(telegramId);
        String key = INVENTORY_BOOSTERS_KEY.getKey(telegramId);
        List<InventoryBoosterDto> boosterDtos = toDtoList(boosters);
        redisTemplate.opsForValue().set(key, boosterDtos);
        redisTemplate.expire(key, 1, TimeUnit.HOURS);
    }

    public Optional<InventoryBooster> getInventoryBooster(Long telegramId, String boosterName) {
        List<InventoryBooster> allBoosters = getInventoryBoosters(telegramId);
        return allBoosters.stream()
                .filter(booster -> booster.getName().name().equals(boosterName))
                .findFirst();
    }

    public List<InventoryBooster> getInventoryBoosters(Long telegramId) {
        String key = INVENTORY_BOOSTERS_KEY.getName() + telegramId;

        if (isInventoryBoostersEmpty(telegramId)) {
            return new ArrayList<>();
        }

        Object boosters = redisTemplate.opsForValue().get(key);

        if (boosters instanceof List) {
            try {
                @SuppressWarnings("unchecked")
                List<InventoryBoosterDto> boosterDtos = (List<InventoryBoosterDto>) boosters;
                return toEntityList(boosterDtos);
            } catch (ClassCastException e) {
                deleteInventoryBoosters(telegramId);
                return new ArrayList<>();
            }
        }

        return new ArrayList<>();
    }

    public List<InventoryBooster> getInventoryBoostersByName(Long telegramId, ResourceType type) {
        List<InventoryBooster> allBoosters = getInventoryBoosters(telegramId);
        return allBoosters.stream()
                .filter(booster -> booster.getName().equals(type))
                .collect(Collectors.toList());
    }

    public void addInventoryBooster(Long telegramId, InventoryBooster booster) {
        String key = INVENTORY_BOOSTERS_KEY.getName() + telegramId;

        // Получаем текущий список бустеров
        List<InventoryBooster> boosters = getInventoryBoosters(telegramId);

        // Добавляем новый бустер
        boosters.add(booster);

        // Сохраняем обновленный список
        List<InventoryBoosterDto> boosterDtos = toDtoList(boosters);
        redisTemplate.opsForValue().set(key, boosterDtos);
        redisTemplate.expire(key, 1, TimeUnit.HOURS);

        // Убираем отметку о пустоте
        clearInventoryBoostersEmptyMark(telegramId);
    }

    public void updateInventoryBooster(Long telegramId, InventoryBooster booster) {
        String key = INVENTORY_BOOSTERS_KEY.getName() + telegramId;

        // Получаем текущий список бустеров
        List<InventoryBooster> boosters = getInventoryBoosters(telegramId);

        // Удаляем старый бустер с таким же именем и характеристиками
        boosters = boosters.stream()
                .filter(b -> !(b.getName().equals(booster.getName()) &&
                        b.getValue().equals(booster.getValue()) &&
                        b.getDurationMilli().equals(booster.getDurationMilli())))
                .collect(Collectors.toList());

        // Добавляем обновленный бустер
        boosters.add(booster);

        // Сохраняем обновленный список
        List<InventoryBoosterDto> boosterDtos = toDtoList(boosters);
        redisTemplate.opsForValue().set(key, boosterDtos);
        redisTemplate.expire(key, 1, TimeUnit.HOURS);

        // Убираем отметку о пустоте
        clearInventoryBoostersEmptyMark(telegramId);
    }

    public void removeInventoryBooster(Long telegramId, InventoryBooster booster) {
        String key = INVENTORY_BOOSTERS_KEY.getName() + telegramId;

        // Получаем текущий список бустеров
        List<InventoryBooster> boosters = getInventoryBoosters(telegramId);

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
            List<InventoryBoosterDto> boosterDtos = toDtoList(boosters);
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