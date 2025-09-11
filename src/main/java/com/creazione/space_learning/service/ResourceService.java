package com.creazione.space_learning.service;

import com.creazione.space_learning.dto.UserDto;
import com.creazione.space_learning.entities.UserEntity;
import com.creazione.space_learning.entities.Building;
import com.creazione.space_learning.game.resources.ReferralBox1;
import com.creazione.space_learning.entities.Resource;
import com.creazione.space_learning.game.resources.ResourceList;
import com.creazione.space_learning.enums.ResourceType;
import com.creazione.space_learning.repository.ResourcesRepository;
import com.creazione.space_learning.service.redis.ResourceCacheService;
import com.creazione.space_learning.service.redis.UserCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ResourceService {
    private final ResourcesRepository resourcesRepository;
    private final UserCacheService userCacheService;
    private final ResourceCacheService resourceCacheService;

    public boolean calculateQuantityChanges(UserDto userDto, Instant date) {
        List<Resource> resources = userDto.getResources();
        List<Building> buildings = userDto.getBuildings();
        long dateLong = date.toEpochMilli();
        boolean isUpdateDB = false;
        //System.out.println("Текущее время: " + new Date(dateLong));
        for (Building building : buildings) {
            boolean flag = false;
            for (Resource resource : resources) {
                if (resource.getName().equals(building.getProduction())) {
                    //System.out.println("Ресурс существует: " + resource.getName() + " - " + resource.getQuantity() + "шт.");
                    isUpdateDB = calculateUpdate(building, resource, dateLong);
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                for (Resource resource : ResourceList.RESOURCES_LIST) {
                    if (building.getProduction().equals(resource.getName())) {
                        //System.out.println("Ресурс не существует, создаётся необходимый ресурс для пользователя.");
                        //System.out.println("- ресурс: " + resource.getName() + " - " + resource.getQuantity() + "шт.");
                        resource.setUserId(userDto.getId());
                        resources.add(resource);
                        //System.out.println("Ресурс не существует, создаётся необходимый ресурс для пользователя.");
                        isUpdateDB = calculateUpdate(building, resource, dateLong);
                        break;
                    }
                }
            }
        }
        return isUpdateDB;
    }

    public boolean calculateQuantityChanges(UserEntity userEntity, Instant date) {
        Set<Resource> resources = userEntity.getResources();
        Set<Building> buildings = userEntity.getBuildings();
        long dateLong = date.toEpochMilli();
        boolean isUpdateDB = false;
        //System.out.println("Текущее время: " + new Date(dateLong));
        for (Building building : buildings) {
            boolean flag = false;
            for (Resource resource : resources) {
                if (resource.getName().equals(building.getProduction())) {
                    //System.out.println("Ресурс существует: " + resource.getName() + " - " + resource.getQuantity() + "шт.");
                    isUpdateDB = calculateUpdate(building, resource, dateLong);
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                for (Resource resource : ResourceList.RESOURCES_LIST) {
                    if (building.getProduction().equals(resource.getName())) {
                        //System.out.println("Ресурс не существует, создаётся необходимый ресурс для пользователя.");
                        //System.out.println("- ресурс: " + resource.getName() + " - " + resource.getQuantity() + "шт.");
                        resource.setUserId(userEntity.getId());
                        resources.add(resource);
                        //System.out.println("Ресурс не существует, создаётся необходимый ресурс для пользователя.");
                        isUpdateDB = calculateUpdate(building, resource, dateLong);
                        break;
                    }
                }
            }
        }
        return isUpdateDB;
    }

    private boolean calculateUpdate(Building building, Resource resource, long date) {
        long timeUpgrade = building.getLastTimeUpgrade().toEpochMilli() + building.getTimeToUpdate();
        //System.out.println("время в которое производится обновление уровня в случае постройки/улучшения строения:");
        //System.out.println(new Date(timeUpgrade));
        // если обновление данных случилось без поднятия уровня
        if (building.getLastUpdate().toEpochMilli() > timeUpgrade) {
            //System.out.println("Последнее обновление было после поднятия уровня в: " + building.getLastUpdate());
            setQuantity(resource, simpleCalculateUpdate(building, resource, date));
            building.setLastUpdate(Instant.now());
            return false;
        } else {
            // если строение ещё не построилось
            // lastUpdate тут меньше или равен timeUpgrade
            if (date >= timeUpgrade) {
                //System.out.println("Текущее время " + new Date(date) + " больше или равно времени обновления уровня: " + new Date(timeUpgrade));
                setQuantity(resource, simpleCalculateUpdate(building, resource, timeUpgrade));
                building.setLastUpdate(Instant.ofEpochMilli(timeUpgrade));
                building.upLevel();
                setQuantity(resource, simpleCalculateUpdate(building, resource, date));
                building.setLastUpdate(Instant.ofEpochMilli(date));
                return true;
            } else {
                //System.out.println("Текущее время " + new Date(date) + " меньше времени обновления уровня: " + new Date(timeUpgrade));
                setQuantity(resource, simpleCalculateUpdate(building, resource, date));
                building.setLastUpdate(Instant.ofEpochMilli(date));
                return false;
            }
        }
    }

    private double simpleCalculateUpdate(Building building, Resource resource, long date) {
        if (building.getLevel() == 0) {
            return 0;
        }
        //System.out.println("Производится вычисление: ");
        //System.out.println("Было ресурса : " + resource.getQuantity() + "шт.");
        //System.out.println("building last update: " + building.getLastUpdate().getTime());
        //System.out.println("building last upgrade: " + building.getLastTimeUpgrade().getTime());
        //System.out.println("date: " + date);
        //System.out.println("new Date(): " + new Date().getTime());
        //System.out.println("date - building.getLastUpdate().getTime(): " + (date - building.getLastUpdate().getTime()));

        // calculateIncrementMining() - это бонус или ограничение от бустеров
        double addResources = ((date - building.getLastUpdate().toEpochMilli()) *
                (building.getQuantityMining() * Math.pow(building.getIncrementMining(), building.getLevel()))) * building.calculateIncrementMining();
        //System.out.println("добавляется " + addResources + " " + resource.getName());
        return addResources;
    }

    private void setQuantity(Resource resource, double addResources) {
        resource.setQuantity(resource.getQuantity() + addResources);
        //System.out.println("теперь ресурсов: " + resource.getQuantity());
    }

    public double getQuantityInHour(Building building) {
        if (building.getLevel() == 0) {
            return 0;
        }
        return 3_600_000 *
                (building.getQuantityMining() * Math.pow(building.getIncrementMining(), building.getLevel()));
    }

    public void addReferralBox1OrIncrement(Set<Resource> resources, ResourceType resourceType) {
        boolean isResourceHere = false;
        for (Resource resource : resources) {
            if (resource.getName().equals(resourceType)) {
                resource.incrementQuantity();
                isResourceHere = true;
            }
        }
        if (!isResourceHere) {
            resources.add(new ReferralBox1(1));
        }
    }

    public Set<Resource> findAllByUserId(long id) {
        return resourcesRepository.findAllByUserId(id);
    }
    public void save(List<Resource> resources) {
        resourcesRepository.saveAll(resources);
    }
    public void saveAll(Set<Resource> resources, long telegramId) {
        userCacheService.deleteFullUser(telegramId);
        resourcesRepository.saveAll(resources);
    }
    public void delete(Resource resource, long telegramId) {
        userCacheService.deleteFullUser(telegramId);
        resourcesRepository.delete(resource);
    }

    public List<Resource> getResources(Long id, Long telegramId) {
        if (resourceCacheService.isResourcesEmpty(telegramId)) {
            return new ArrayList<>();
        }
        List<Resource> resources = resourceCacheService.getResources(telegramId);
        if (!resources.isEmpty()) {
            return resources;
        }
        List<Resource> result = resourcesRepository.findAllByUserId(id).stream().toList();
        resourceCacheService.cacheResources(telegramId, result);
        return result;
    }
}
