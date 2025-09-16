package com.creazione.space_learning.service.redis;

import com.creazione.space_learning.entities.postgres.ResourceP;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.creazione.space_learning.service.redis.CacheKey.*;

@Service
@RequiredArgsConstructor
public class ResourceCacheService {
    private final RedisTemplate<String, Object> redisTemplate;

    public void cacheResources(Long userId, List<ResourceP> resources) {
        deleteResources(userId);
        String key = RESOURCE_KEY_PREFIX.getName() + userId;

        if (resources != null && !resources.isEmpty()) {
            // Сохраняем весь список ресурсов как единый объект
            redisTemplate.opsForValue().set(key, resources);
            redisTemplate.expire(key, 1, TimeUnit.HOURS);
        } else {
            markResourcesAsEmpty(userId);
        }
    }

    public boolean hasResources(Long userId) {
        return redisTemplate.hasKey(RESOURCE_KEY_PREFIX.getName() + userId);
    }

    public List<ResourceP> getResources(Long userId) {
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
                List<ResourceP> result = (List<ResourceP>) resources;
                return result;
            } catch (ClassCastException e) {
                // Если произошла ошибка приведения типа, очищаем кэш
                deleteResources(userId);
                return List.of();
            }
        }

        return List.of();
    }

    public void updateSingleResource(Long userId, ResourceP resource) {
        String key = RESOURCE_KEY_PREFIX.getName() + userId;

        // Получаем текущий список ресурсов
        List<ResourceP> resources = getResources(userId);

        // Удаляем старый ресурс с таким же именем (если есть)
        resources = resources.stream()
                .filter(r -> !r.getName().equals(resource.getName()))
                .collect(Collectors.toList());

        // Добавляем обновленный ресурс
        resources.add(resource);

        // Сохраняем обновленный список
        redisTemplate.opsForValue().set(key, resources);
        redisTemplate.expire(key, 1, TimeUnit.HOURS);

        // Убираем отметку о пустоте
        clearResourcesEmptyMark(userId);
    }

    public void deleteResources(Long telegramId) {
        //System.out.println("deleteResources(): " + telegramId);
        clearResourcesEmptyMark(telegramId);
        redisTemplate.delete(RESOURCE_KEY_PREFIX.getName() + telegramId);
    }

    // Методы для установки пустых ключей
    public void markResourcesAsEmpty(Long telegramId) {
        //System.out.println("markResourcesAsEmpty(): " + telegramId);
        redisTemplate.opsForValue().set(EMPTY_RESOURCES_KEY.getName() + telegramId, "true", 5, TimeUnit.MINUTES);
    }

    // Методы для проверки пустых ключей
    public boolean isResourcesEmpty(Long telegramId) {
        //System.out.println("isResourcesEmpty(): " + telegramId);
        return Boolean.TRUE.equals(redisTemplate.hasKey(EMPTY_RESOURCES_KEY.getName() + telegramId));
    }

    // Методы для удаления пустых ключей (при добавлении данных)
    public void clearResourcesEmptyMark(Long telegramId) {
        //System.out.println("clearResourcesEmptyMark(): " + telegramId);
        redisTemplate.delete(EMPTY_RESOURCES_KEY.getName() + telegramId);
    }
}
