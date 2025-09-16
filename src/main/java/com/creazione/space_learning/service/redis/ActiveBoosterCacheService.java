package com.creazione.space_learning.service.redis;

import com.creazione.space_learning.entities.redis.ActiveBoosterR;
import com.creazione.space_learning.entities.postgres.ActiveBoosterP;
import com.creazione.space_learning.enums.ResourceType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.creazione.space_learning.service.redis.CacheKey.*;

@Service
@RequiredArgsConstructor
public class ActiveBoosterCacheService {
    private final RedisTemplate<String, Object> redisTemplate;

    // Методы преобразования
    private ActiveBoosterR toRedisObject(ActiveBoosterP booster) {
        return new ActiveBoosterR(
                booster.getId(),
                booster.getUserId(),
                booster.getName(),
                booster.getValue(),
                booster.getEndsAt(),
                booster.getStartsAt()
        );
    }

    private ActiveBoosterP toGameObject(ActiveBoosterR dto) {
        ActiveBoosterP booster = new ActiveBoosterP();
        booster.setId(dto.getId());
        booster.setUserId(dto.getUserId());
        booster.setName(dto.getName());
        booster.setValue(dto.getValue());
        booster.setEndsAt(dto.getEndsAt());
        booster.setStartsAt(dto.getStartsAt());
        return booster;
    }

    private List<ActiveBoosterR> toRedisObjectList(List<ActiveBoosterP> boosters) {
        return boosters.stream()
                .map(this::toRedisObject)
                .collect(Collectors.toList());
    }

    private List<ActiveBoosterP> toGameObjectList(List<ActiveBoosterR> boosterDtos) {
        return boosterDtos.stream()
                .map(this::toGameObject)
                .collect(Collectors.toList());
    }

    public void cacheActiveBoosters(Long telegramId, List<ActiveBoosterP> boosters) {
        deleteActiveBoosters(telegramId);
        String key = ACTIVE_BOOSTERS_KEY.getKey(telegramId);
        List<ActiveBoosterR> boosterDtos = toRedisObjectList(boosters);
        redisTemplate.opsForValue().set(key, boosterDtos);
        redisTemplate.expire(key, 1, TimeUnit.HOURS);
    }

    public List<ActiveBoosterP> getActiveBoosters(Long telegramId) {
        String key = ACTIVE_BOOSTERS_KEY.getName() + telegramId;

        if (isActiveBoostersEmpty(telegramId)) {
            return new ArrayList<>();
        }

        Object boosters = redisTemplate.opsForValue().get(key);

        if (boosters instanceof List) {
            try {
                @SuppressWarnings("unchecked")
                List<ActiveBoosterR> boosterDtos = (List<ActiveBoosterR>) boosters;
                return toGameObjectList(boosterDtos);
            } catch (ClassCastException e) {
                deleteActiveBoosters(telegramId);
                return new ArrayList<>();
            }
        }

        return new ArrayList<>();
    }

    public List<ActiveBoosterP> getActiveBoostersByNameIn(Long telegramId, List<ResourceType> types) {
        List<String> boostersTypeString = types.stream().map(ResourceType::getName).toList();
        List<ActiveBoosterP> allBoosters = getActiveBoosters(telegramId);
        return allBoosters.stream()
                .filter(booster -> boostersTypeString.contains(booster.getName().getName()))
                .collect(Collectors.toList());
    }

    public void addActiveBooster(Long telegramId, ActiveBoosterP booster) {
        String key = ACTIVE_BOOSTERS_KEY.getName() + telegramId;

        // Получаем текущий список бустеров
        List<ActiveBoosterP> boosters = getActiveBoosters(telegramId);

        // Добавляем новый бустер
        boosters.add(booster);

        // Сохраняем обновленный список
        List<ActiveBoosterR> boosterDtos = toRedisObjectList(boosters);
        redisTemplate.opsForValue().set(key, boosterDtos);
        redisTemplate.expire(key, 1, TimeUnit.HOURS);

        // Убираем отметку о пустоте
        clearActiveBoostersEmptyMark(telegramId);
    }

    public void removeActiveBooster(Long telegramId, ActiveBoosterP booster) {
        String key = ACTIVE_BOOSTERS_KEY.getName() + telegramId;

        // Получаем текущий список бустеров
        List<ActiveBoosterP> boosters = getActiveBoosters(telegramId);

        // Удаляем бустер с такими же характеристиками
        boosters = boosters.stream()
                .filter(b -> !(b.getName().equals(booster.getName()) &&
                        b.getValue().equals(booster.getValue()) &&
                        b.getStartsAt().equals(booster.getStartsAt()) &&
                        b.getEndsAt().equals(booster.getEndsAt())))
                .collect(Collectors.toList());

        // Сохраняем обновленный список
        if (boosters.isEmpty()) {
            markActiveBoostersAsEmpty(telegramId);
        } else {
            List<ActiveBoosterR> boosterDtos = toRedisObjectList(boosters);
            redisTemplate.opsForValue().set(key, boosterDtos);
            redisTemplate.expire(key, 1, TimeUnit.HOURS);
        }
    }

    public boolean hasActiveBooster(Long telegramId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(ACTIVE_BOOSTERS_KEY.getKey(telegramId)));
    }

    public void deleteActiveBoosters(Long telegramId) {
        clearActiveBoostersEmptyMark(telegramId);
        redisTemplate.delete(ACTIVE_BOOSTERS_KEY.getKey(telegramId));
    }

    public void markActiveBoostersAsEmpty(Long telegramId) {
        redisTemplate.opsForValue().set(EMPTY_ACTIVE_BOOSTERS_KEY.getName() + telegramId, "true", 5, TimeUnit.MINUTES);
    }

    public boolean isActiveBoostersEmpty(Long telegramId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(EMPTY_ACTIVE_BOOSTERS_KEY.getName() + telegramId));
    }

    public void clearActiveBoostersEmptyMark(Long telegramId) {
        redisTemplate.delete(EMPTY_ACTIVE_BOOSTERS_KEY.getKey(telegramId));
    }
}