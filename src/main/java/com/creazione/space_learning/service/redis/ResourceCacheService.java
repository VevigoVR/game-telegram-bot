package com.creazione.space_learning.service.redis;

import com.creazione.space_learning.entities.game_entity.ResourceDto;
import com.creazione.space_learning.entities.redis.ResourceR;
import com.creazione.space_learning.game.resources.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.creazione.space_learning.service.redis.CacheKey.*;

@Service
@RequiredArgsConstructor
public class ResourceCacheService {
    private final RedisTemplate<String, Object> redisTemplate;

    public List<ResourceR> toRedisObjectList(List<ResourceDto> resourceDtoList) {
        List<ResourceR> resourceRList = new ArrayList<>();
        for (ResourceDto resource : resourceDtoList) {
            resourceRList.add(toRedisObject(resource));
        }
        return resourceRList;
    }

    public List<ResourceDto> toGameObjectList(List<ResourceR> resourceRList) {
        List<ResourceDto> resourceDtoList = new ArrayList<>();
        for (ResourceR resource : resourceRList) {
            resourceDtoList.add(toGameObject(resource));
        }
        return resourceDtoList;
    }

    private ResourceR toRedisObject(ResourceDto resourceDto) {
        return new ResourceR(
                resourceDto.getId(),
                resourceDto.getUserId(),
                resourceDto.getName(),
                resourceDto.getEmoji(),
                resourceDto.getQuantity()
        );
    }

    private ResourceDto toGameObject(ResourceR resourceR) {
        return switch (resourceR.getName()) {
            case COIN -> new Coin(resourceR.getId(), resourceR.getUserId(), resourceR.getQuantity());
            case CRYPTO -> new Crypto(resourceR.getId(), resourceR.getUserId(), resourceR.getQuantity());
            case GOLD -> new Gold(resourceR.getId(), resourceR.getUserId(), resourceR.getQuantity());
            case KNOWLEDGE -> new Knowledge(resourceR.getId(), resourceR.getUserId(), resourceR.getQuantity());
            case LOOT_BOX_COMMON -> new LootBoxCommon(resourceR.getId(), resourceR.getUserId(), resourceR.getQuantity());
            case LOOT_BOX_RARE -> new LootBoxRare(resourceR.getId(), resourceR.getUserId(), resourceR.getQuantity());
            case METAL -> new Metal(resourceR.getId(), resourceR.getUserId(), resourceR.getQuantity());
            case REFERRAL_BOX_1 -> new ReferralBox1(resourceR.getId(), resourceR.getUserId(), resourceR.getQuantity());
            case REFERRAL_BOX_2 -> new ReferralBox2(resourceR.getId(), resourceR.getUserId(), resourceR.getQuantity());
            case REFERRAL_BOX_3 -> new ReferralBox3(resourceR.getId(), resourceR.getUserId(), resourceR.getQuantity());
            case STONE -> new Stone(resourceR.getId(), resourceR.getUserId(), resourceR.getQuantity());
            case WOOD -> new Wood(resourceR.getId(), resourceR.getUserId(), resourceR.getQuantity());
            case UNKNOWN -> new Unknown(resourceR.getId(), resourceR.getUserId(), resourceR.getQuantity());
            // Добавьте другие типы ресурсов
            default -> new Unknown(resourceR.getId(), resourceR.getUserId(), resourceR.getQuantity());
        };
    }

    public void cacheResources(Long userId, List<ResourceDto> resourcesDto) {
        List<ResourceR> resources = toRedisObjectList(resourcesDto);
        deleteResources(userId);
        String key = RESOURCE_KEY_PREFIX.getName() + userId;

        if (!resources.isEmpty()) {
            // Сохраняем весь список ресурсов как единый объект
            redisTemplate.opsForValue().set(key, resources);
            redisTemplate.expire(key, 1, TimeUnit.HOURS);
        } else {
            markResourcesAsEmpty(userId);
        }
    }

    public boolean hasResources(Long userId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(RESOURCE_KEY_PREFIX.getName() + userId));
    }

    public List<ResourceDto> getResources(Long userId) {
        String key = RESOURCE_KEY_PREFIX.getName() + userId;

        // Проверяем, есть ли отметка о пустоте
        if (isResourcesEmpty(userId)) {
            return List.of();
        }

        // Получаем весь список ресурсов
        Object resources = redisTemplate.opsForValue().get(key);

        if (resources instanceof List) {
            try {
                @SuppressWarnings("unchecked")
                List<ResourceR> result = (List<ResourceR>) resources;
                return toGameObjectList(result);
            } catch (ClassCastException e) {
                // Если произошла ошибка приведения типа, очищаем кэш
                deleteResources(userId);
                return List.of();
            }
        }

        return List.of();
    }

    public void deleteResources(Long telegramId) {
        clearResourcesEmptyMark(telegramId);
        redisTemplate.delete(RESOURCE_KEY_PREFIX.getName() + telegramId);
    }

    // Методы для установки пустых ключей
    public void markResourcesAsEmpty(Long telegramId) {
        redisTemplate.opsForValue().set(EMPTY_RESOURCES_KEY.getName() + telegramId, "true", 5, TimeUnit.MINUTES);
    }

    // Методы для проверки пустых ключей
    public boolean isResourcesEmpty(Long telegramId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(EMPTY_RESOURCES_KEY.getName() + telegramId));
    }

    // Методы для удаления пустых ключей (при добавлении данных)
    public void clearResourcesEmptyMark(Long telegramId) {
        redisTemplate.delete(EMPTY_RESOURCES_KEY.getName() + telegramId);
    }
}
