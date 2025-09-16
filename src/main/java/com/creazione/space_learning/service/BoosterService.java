package com.creazione.space_learning.service;

import com.creazione.space_learning.dto.UserDto;
import com.creazione.space_learning.entities.postgres.ActiveBoosterP;
import com.creazione.space_learning.entities.postgres.BuildingP;
import com.creazione.space_learning.entities.postgres.InventoryBoosterP;
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

    public double getIncrementMining(BuildingP building, Instant lastMiningUpdate) {
        long userId = building.getUserId();
        Instant now = Instant.now();
        long durationMillis = Duration.between(lastMiningUpdate, now).toMillis();

        if (durationMillis == 0) {
            return 0.0;
        }

        long telegramId = getTelegramId(userId);

        double totalBoost = 0.0;
        List<ActiveBoosterP> activeBoosters = new ArrayList<>();

        switch (building.getName()) {
            case GOLD_BUILDING -> activeBoosters.addAll(findAllABByUserIdAndNameIn(userId, telegramId, ResourceType.getGoldBoosters()));
            case STONE_BUILDING -> activeBoosters.addAll(findAllABByUserIdAndNameIn(userId, telegramId, ResourceType.getStoneBoosters()));
            case METAL_BUILDING -> activeBoosters.addAll(findAllABByUserIdAndNameIn(userId, telegramId, ResourceType.getMetalBoosters()));
            case WOOD_BUILDING -> activeBoosters.addAll(findAllABByUserIdAndNameIn(userId, telegramId, ResourceType.getWoodBoosters()));
        }
        activeBoosters.addAll(findAllABByUserIdAndNameIn(userId, telegramId, ResourceType.getGeneralBoosters()));

        for (ActiveBoosterP booster : activeBoosters) {
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

    public List<ActiveBoosterP> findAllABByUserIdAndNameIn(long userId, long telegramId, List<ResourceType> types) {
        boolean isCached = activeBoosterCacheService.hasActiveBooster(telegramId);
        //System.out.println("isCached in findAllABByUserIdAndNameIn BoosterService.class: " + isCached);

        if (activeBoosterCacheService.isActiveBoostersEmpty(telegramId)) {
            //System.out.println("activeBoosterCacheService.isActiveBoostersEmpty(telegramId): " + true);
            return new ArrayList<>();
        }
        //System.out.println("activeBoosterCacheService.isActiveBoostersEmpty(telegramId): " + false);
        List<ActiveBoosterP> boosters = activeBoosterCacheService.getActiveBoostersByNameIn(telegramId, types);
        if (boosters != null && !boosters.isEmpty()) {
            return boosters;
        }

        if (!isCached) {
            List<ActiveBoosterP> allActiveBoosters = activeBoosterRepository.findAllByUserId(userId);
            if (!allActiveBoosters.isEmpty()) {
                activeBoosterCacheService.cacheActiveBoosters(telegramId, allActiveBoosters);
                return allActiveBoosters
                        .stream()
                        .filter(booster -> types.contains(booster.getName()))
                        .collect(Collectors.toList());
            } else {
                activeBoosterCacheService.markActiveBoostersAsEmpty(telegramId);
                //System.out.println("никаких актив бустеров нет");
                return new ArrayList<>();
            }
        } else {
            return new ArrayList<>();
        }
    }

    public Set<InventoryBoosterP> findAllIBByUserId(UserDto userDto) {
        if (inventoryBoosterCacheService.isInventoryBoostersEmpty(userDto.getTelegramId())) {
            return new HashSet<>();
        }
        Set<InventoryBoosterP> boosters = new HashSet<>(inventoryBoosterCacheService
                .getInventoryBoosters(userDto.getTelegramId())
        );
        if (!boosters.isEmpty()) {
            return boosters;
        }
        Set<InventoryBoosterP> result = inventoryBoosterRepository.findAllByUserId(userDto.getTelegramId());
        if (!result.isEmpty()) {
            inventoryBoosterCacheService.cacheInventoryBoosters(userDto.getTelegramId(), result.stream().toList());
        } else {
            inventoryBoosterCacheService.markInventoryBoostersAsEmpty(userDto.getTelegramId());
        }
        return result;
    }

    public List<InventoryBoosterP> findAllIBByUserIdToList(Long id, Long telegramId) {
        if (inventoryBoosterCacheService.isInventoryBoostersEmpty(telegramId)) {
            return new ArrayList<>();
        }
        List<InventoryBoosterP> boosters = inventoryBoosterCacheService.getInventoryBoosters(telegramId);
        if (!boosters.isEmpty()) {
            return boosters;
        }
        Set<InventoryBoosterP> result = inventoryBoosterRepository.findAllByUserId(id);
        if (!result.isEmpty()) {
            inventoryBoosterCacheService.cacheInventoryBoosters(telegramId, result.stream().toList());
        } else {
            inventoryBoosterCacheService.markInventoryBoostersAsEmpty(telegramId);
        }
        return result.stream().toList();
    }

    public Set<InventoryBoosterP> findAllIBByUserIdAndNameAndValueAndDurationMilli(Long userId, Long telegramId, ResourceType type, Double value, Long durationMilli) {
        boolean isCached = inventoryBoosterCacheService.hasInventoryBooster(telegramId);

        if (inventoryBoosterCacheService.isInventoryBoostersEmpty(telegramId)) {
            return new HashSet<>();
        }
        Set<InventoryBoosterP> boosters = new HashSet<>(inventoryBoosterCacheService
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
            Set<InventoryBoosterP> allInventoryBoosters = inventoryBoosterRepository.findAllByUserId(userId);
            if (!allInventoryBoosters.isEmpty()) {
                inventoryBoosterCacheService.cacheInventoryBoosters(telegramId, allInventoryBoosters.stream().toList());
                return allInventoryBoosters
                        .stream()
                        .filter(booster ->
                                booster.getValue().equals(value)
                                        && booster.getDurationMilli().equals(durationMilli)
                                        && booster.getUserId().equals(userId)
                                        && booster.getName().equals(type))
                        .collect(Collectors.toSet());
            } else {
                inventoryBoosterCacheService.markInventoryBoostersAsEmpty(telegramId);
                return new HashSet<>();
            }
        } else {
            return new HashSet<>();
        }
    }

    public void saveAllIB(Set<InventoryBoosterP> boosters, long telegramId) {
        inventoryBoosterRepository.saveAll(boosters);
        inventoryBoosterCacheService.deleteInventoryBoosters(telegramId);
        //inventoryBoosterCacheService.cacheInventoryBoosters(telegramId, boosters.stream().toList());
    }

    public void saveIB(InventoryBoosterP booster, long telegramId) {
        inventoryBoosterRepository.save(booster);
        inventoryBoosterCacheService.deleteInventoryBoosters(telegramId);
        //inventoryBoosterCacheService.cacheInventoryBoosters(telegramId, List.of(booster));
    }

    public void saveAllAB(Set<ActiveBoosterP> boosters, long telegramId, long userId) {
        activeBoosterRepository.saveAll(boosters);
        activeBoosterCacheService.deleteActiveBoosters(telegramId);
        //activeBoosterCacheService.cacheActiveBoosters(telegramId,activeBoosterRepository.findAllByUserId(userId));
    }

    public void saveAB(ActiveBoosterP booster, long telegramId, long userId) {
        activeBoosterRepository.save(booster);
        activeBoosterCacheService.deleteActiveBoosters(telegramId);
        //activeBoosterCacheService.cacheActiveBoosters(telegramId,activeBoosterRepository.findAllByUserId(userId));
    }

    public void deleteIB(InventoryBoosterP booster, long telegramId) {
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