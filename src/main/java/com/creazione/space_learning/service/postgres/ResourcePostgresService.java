package com.creazione.space_learning.service.postgres;

import com.creazione.space_learning.entities.game_entity.ResourceDto;
import com.creazione.space_learning.entities.postgres.ResourceP;
import com.creazione.space_learning.game.resources.*;
import com.creazione.space_learning.repository.ResourcesRepository;
import com.creazione.space_learning.service.redis.ResourceCacheService;
import com.creazione.space_learning.service.redis.UserCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ResourcePostgresService {
    private final ResourcesRepository resourcesRepository;
    private final UserCacheService userCacheService;
    private final ResourceCacheService resourceCacheService;

    public List<ResourceP> toPostgresObjectList(List<ResourceDto> resourceDtoList) {
        List<ResourceP> resourcePList = new ArrayList<>();
        for (ResourceDto resource : resourceDtoList) {
            resourcePList.add(toPostgresObject(resource));
        }
        return resourcePList;
    }

    public List<ResourceDto> toGameObjectList(Set<ResourceP> resourcePList) {
        List<ResourceDto> resourceDtoList = new ArrayList<>();
        for (ResourceP resource : resourcePList) {
            resourceDtoList.add(toGameObject(resource));
        }
        return resourceDtoList;
    }

    private ResourceP toPostgresObject(ResourceDto resourceDto) {
        return new ResourceP(
                resourceDto.getId(),
                resourceDto.getUserId(),
                resourceDto.getName(),
                resourceDto.getEmoji(),
                resourceDto.getQuantity()
        );
    }

    private ResourceDto toGameObject(ResourceP resourceP) {
        return switch (resourceP.getName()) {
            case COIN -> new Coin(resourceP.getId(), resourceP.getUserId(), resourceP.getQuantity());
            case CRYPTO -> new Crypto(resourceP.getId(), resourceP.getUserId(), resourceP.getQuantity());
            case GOLD -> new Gold(resourceP.getId(), resourceP.getUserId(), resourceP.getQuantity());
            case KNOWLEDGE -> new Knowledge(resourceP.getId(), resourceP.getUserId(), resourceP.getQuantity());
            case LOOT_BOX_COMMON -> new LootBoxCommon(resourceP.getId(), resourceP.getUserId(), resourceP.getQuantity());
            case LOOT_BOX_RARE -> new LootBoxRare(resourceP.getId(), resourceP.getUserId(), resourceP.getQuantity());
            case METAL -> new Metal(resourceP.getId(), resourceP.getUserId(), resourceP.getQuantity());
            case REFERRAL_BOX_1 -> new ReferralBox1(resourceP.getId(), resourceP.getUserId(), resourceP.getQuantity());
            case REFERRAL_BOX_2 -> new ReferralBox2(resourceP.getId(), resourceP.getUserId(), resourceP.getQuantity());
            case REFERRAL_BOX_3 -> new ReferralBox3(resourceP.getId(), resourceP.getUserId(), resourceP.getQuantity());
            case STONE -> new Stone(resourceP.getId(), resourceP.getUserId(), resourceP.getQuantity());
            case WOOD -> new Wood(resourceP.getId(), resourceP.getUserId(), resourceP.getQuantity());
            case UNKNOWN -> new Unknown(resourceP.getId(), resourceP.getUserId(), resourceP.getQuantity());
            // Добавьте другие типы ресурсов
            default -> new Unknown(resourceP.getId(), resourceP.getUserId(), resourceP.getQuantity());
        };
    }

    public List<ResourceDto> findAllByUserId(long id) {
        return toGameObjectList(new HashSet<>(resourcesRepository.findAllByUserId(id)));
    }

    public void save(List<ResourceDto> resources) {
        resourcesRepository.saveAll(toPostgresObjectList(resources));
    }

    public void saveAll(List<ResourceDto> resources, long telegramId) {
        resourceCacheService.deleteResources(telegramId);
        resourcesRepository.saveAll(toPostgresObjectList(resources));
    }
    public void delete(ResourceDto resource, long telegramId) {
        resourceCacheService.deleteResources(telegramId);
        resourcesRepository.delete(toPostgresObject(resource));
    }

    public List<ResourceDto> findResourcesById(Long id, Long telegramId) {
        if (resourceCacheService.isResourcesEmpty(telegramId)) {
            return new ArrayList<>();
        }
        List<ResourceDto> resources = resourceCacheService.getResources(telegramId);
        if (!resources.isEmpty()) {
            return resources;
        }
        List<ResourceP> result = resourcesRepository.findAllByUserId(id).stream().toList();
        Set<ResourceP> resultSet = new HashSet<>(result);
        resourceCacheService.cacheResources(telegramId, toGameObjectList(resultSet));
        return toGameObjectList(resultSet);
    }
}
