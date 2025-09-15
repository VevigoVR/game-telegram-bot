package com.creazione.space_learning.service;

import com.creazione.space_learning.dto.UserDto;
import com.creazione.space_learning.entities.ActiveBooster;
import com.creazione.space_learning.entities.Building;
import com.creazione.space_learning.entities.InventoryBooster;
import com.creazione.space_learning.enums.ResourceType;
import com.creazione.space_learning.repository.ActiveBoosterRepository;
import com.creazione.space_learning.repository.InventoryBoosterRepository;
import com.creazione.space_learning.service.postgres.UserService;
import com.creazione.space_learning.service.redis.ActiveBoosterCacheService;
import com.creazione.space_learning.service.redis.IdTelegramCacheService;
import com.creazione.space_learning.service.redis.InventoryBoosterCacheService;
import com.creazione.space_learning.service.redis.UserCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoosterService {
    private final ActiveBoosterRepository activeBoosterRepository;
    private final InventoryBoosterRepository inventoryBoosterRepository;
    private final UserCacheService userCacheService;
    private final InventoryBoosterCacheService inventoryBoosterCacheService;
    private final ActiveBoosterCacheService activeBoosterCacheService;
    private final IdTelegramCacheService idTelegramCacheService;
    private final UserService userService;

    public double getIncrementMining(Building building, Instant lastMiningUpdate) {
        long userId = building.getUserId();
        Instant now = Instant.now();
        long durationMillis = Duration.between(lastMiningUpdate, now).toMillis();

        if (durationMillis == 0) {
            return 0.0;
        }

        long telegramId = getTelegramId(userId);

        double totalBoost = 0.0;
        List<ActiveBooster> activeBoosters = new ArrayList<>();

        switch (building.getName()) {
            case GOLD_BUILDING -> activeBoosters = findAllABByUserIdAndNameIn(userId, telegramId, ResourceType.getGoldBoosters());
            case STONE_BUILDING -> activeBoosters = findAllABByUserIdAndNameIn(userId, telegramId, ResourceType.getStoneBoosters());
            case METAL_BUILDING -> activeBoosters = findAllABByUserIdAndNameIn(userId, telegramId, ResourceType.getMetalBoosters());
            case WOOD_BUILDING -> activeBoosters = findAllABByUserIdAndNameIn(userId, telegramId, ResourceType.getWoodBoosters());
        }
        activeBoosters.addAll(findAllABByUserIdAndNameIn(userId, telegramId, ResourceType.getGeneralBoosters()));

        for (ActiveBooster booster : activeBoosters) {
            // Определяем период пересечения буста и интервала добычи
            Instant boostStart = booster.getStartsAt();
            Instant boostEnd = booster.getEndsAt();

            Instant effectiveStart = lastMiningUpdate.isBefore(boostStart) ? boostStart : lastMiningUpdate;
            Instant effectiveEnd = now.isAfter(boostEnd) ? boostEnd : now;

            // Если есть пересечение во времени
            if (effectiveStart.isBefore(effectiveEnd)) {
                long boostDurationMillis = Duration.between(effectiveStart, effectiveEnd).toMillis();
                double boostContribution = booster.getValue() * (boostDurationMillis / (double) durationMillis);

                totalBoost += boostContribution;
            }
        }

        return 1 + totalBoost;
    }

    public List<ActiveBooster> findAllABByUserIdAndNameIn(long userId, long telegramId, List<ResourceType> types) {
        boolean isCached = activeBoosterCacheService.hasActiveBooster(telegramId);
        System.out.println("isCached in findAllABByUserIdAndNameIn BoosterService.class: " + isCached);

        if (activeBoosterCacheService.isActiveBoostersEmpty(telegramId)) {
            //System.out.println("activeBoosterCacheService.isActiveBoostersEmpty(telegramId): " + true);
            return new ArrayList<>();
        }
        //System.out.println("activeBoosterCacheService.isActiveBoostersEmpty(telegramId): " + false);
        List<ActiveBooster> boosters = activeBoosterCacheService.getActiveBoostersByNameIn(telegramId, types);
        if (boosters != null && !boosters.isEmpty()) {
            return boosters;
        }

        if (!isCached) {
            List<ActiveBooster> result = activeBoosterRepository.findAllByUserIdAndNameIn(userId, types);
            if (!result.isEmpty()) {
                activeBoosterCacheService.cacheActiveBoosters(telegramId, result);
            } else {
                activeBoosterCacheService.markActiveBoostersAsEmpty(telegramId);
            }
            return result;
        } else {
            return new ArrayList<>();
        }
    }

    public Set<InventoryBooster> findAllIBByUserId(UserDto userDto) {
        if (inventoryBoosterCacheService.isInventoryBoostersEmpty(userDto.getTelegramId())) {
            return new HashSet<>();
        }
        Set<InventoryBooster> boosters = new HashSet<>(inventoryBoosterCacheService
                .getInventoryBoosters(userDto.getTelegramId())
        );
        if (!boosters.isEmpty()) {
            return boosters;
        }
        Set<InventoryBooster> result = inventoryBoosterRepository.findAllByUserId(userDto.getTelegramId());
        inventoryBoosterCacheService.cacheInventoryBoosters(userDto.getTelegramId(), result.stream().toList());
        return result;
    }

    public List<InventoryBooster> findAllIBByUserIdToList(Long id, Long telegramId) {
        if (inventoryBoosterCacheService.isInventoryBoostersEmpty(telegramId)) {
            return new ArrayList<>();
        }
        List<InventoryBooster> boosters = inventoryBoosterCacheService.getInventoryBoosters(telegramId);
        if (!boosters.isEmpty()) {
            return boosters;
        }
        Set<InventoryBooster> result = inventoryBoosterRepository.findAllByUserId(id);
        inventoryBoosterCacheService.cacheInventoryBoosters(telegramId, result.stream().toList());
        return result.stream().toList();
    }

    // НУЖНО СДЕЛАТЬ ПРОВЕРКУ, ЕСТЬ ЛИ У ПОЛЬЗОВАТЕЛЯ ВООБЩЕ БУСТЕРЫ И ЕСЛИ НЕТ, ТО ТОЛЬКО ТОГДА ДОПУСКАТЬ К POSTGRESQL
    public Set<InventoryBooster> findAllIBByUserIdAndNameAndValueAndDurationMilli(Long userId, Long telegramId, ResourceType type, Double value, Long durationMilli) {
        boolean isCached = inventoryBoosterCacheService.hasInventoryBooster(telegramId);

        if (inventoryBoosterCacheService.isInventoryBoostersEmpty(telegramId)) {
            return new HashSet<>();
        }
        Set<InventoryBooster> boosters = new HashSet<>(inventoryBoosterCacheService
                .getInventoryBoostersByName(telegramId, type)
        );
        boosters = boosters
                .stream()
                .filter(booster ->
                    booster.getValue().equals(value)
                            && booster.getDurationMilli().equals(durationMilli)
                            && booster.getUserId().equals(userId)
                            && booster.getName().equals(type))
                .collect(Collectors.toSet());
        if (!boosters.isEmpty()) {
            return boosters;
        }
        if (!isCached) {
            Set<InventoryBooster> result = inventoryBoosterRepository.findAllByUserIdAndNameAndValueAndDurationMilli(userId, type, value, durationMilli);
            inventoryBoosterCacheService.cacheInventoryBoosters(telegramId, result.stream().toList());
            return result;
        } else {
            return new HashSet<>();
        }
    }

    public void saveAllIB(Set<InventoryBooster> boosters, long telegramId) {
        inventoryBoosterRepository.saveAll(boosters);
        inventoryBoosterCacheService.cacheInventoryBoosters(telegramId, boosters.stream().toList());
    }

    public void saveIB(InventoryBooster booster, long telegramId) {
        inventoryBoosterRepository.save(booster);
        inventoryBoosterCacheService.cacheInventoryBoosters(telegramId, List.of(booster));
    }

    public void saveAllAB(Set<ActiveBooster> boosters, long telegramId, long userId) {
        activeBoosterRepository.saveAll(boosters);
        activeBoosterCacheService.cacheActiveBoosters(telegramId,activeBoosterRepository.findAllByUserId(userId));
    }

    public void saveAB(ActiveBooster booster, long telegramId, long userId) {
        activeBoosterRepository.save(booster);
        activeBoosterCacheService.cacheActiveBoosters(telegramId,activeBoosterRepository.findAllByUserId(userId));
    }

    public void deleteIB(InventoryBooster booster, long telegramId) {
        inventoryBoosterCacheService.deleteInventoryBoosters(telegramId);
        inventoryBoosterRepository.delete(booster);
    }

    private long getTelegramId(Long userId) {
        Optional<Long> telegramIdFromCache = idTelegramCacheService.findTelegramIdById(userId);

        if (telegramIdFromCache.isPresent()) {
            return telegramIdFromCache.get();
        }

        Optional<Long> telegramId = userService.findTelegramIdByUserId(userId);
        return telegramId.orElse(0L);
    }
}