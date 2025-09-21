package com.creazione.space_learning.service;

import com.creazione.space_learning.dto.TransferResult;
import com.creazione.space_learning.dto.UserDto;
import com.creazione.space_learning.entities.postgres.BuildingP;
import com.creazione.space_learning.game.resources.Gold;
import com.creazione.space_learning.game.resources.ReferralBox1;
import com.creazione.space_learning.entities.postgres.ResourceP;
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
        List<ResourceP> resources = userDto.getResources();
        List<BuildingP> buildings = userDto.getBuildings();
        long dateLong = date.getEpochSecond();
        boolean isUpdateDB = false;
        //System.out.println("Текущее время: " + new Date(dateLong));
        for (BuildingP building : buildings) {
            boolean flag = false;
            for (ResourceP resource : resources) {
                if (resource.getName().equals(building.getProduction())) {
                    //System.out.println("Ресурс существует: " + resource.getName() + " - " + resource.getQuantity() + "шт.");
                    isUpdateDB = calculateUpdate(building, resource, dateLong);
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                for (ResourceP resource : ResourceList.RESOURCES_LIST) {
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

    private boolean calculateUpdate(BuildingP building, ResourceP resource, long date) {
        long timeUpgrade = building.getLastTimeUpgrade().getEpochSecond() + building.getTimeToUpdate();
        //System.out.println("время в которое производится обновление уровня в случае постройки/улучшения строения:");
        //System.out.println(new Date(timeUpgrade));
        // если обновление данных случилось без поднятия уровня
        if (building.getLastUpdate().getEpochSecond() > timeUpgrade) {
            //System.out.println("Последнее обновление было после поднятия уровня в: " + building.getLastUpdate());
            setQuantity(building, simpleCalculateUpdate(building, resource, date));
            building.setLastUpdate(Instant.now());
            return false;
        } else {
            // если строение ещё не построилось
            // lastUpdate тут меньше или равен timeUpgrade
            if (date >= timeUpgrade) {
                //System.out.println("Текущее время " + new Date(date) + " больше или равно времени обновления уровня: " + new Date(timeUpgrade));
                setQuantity(building, simpleCalculateUpdate(building, resource, timeUpgrade));
                building.setLastUpdate(Instant.ofEpochSecond(timeUpgrade));
                building.upLevel();
                setQuantity(building, simpleCalculateUpdate(building, resource, date));
                building.setLastUpdate(Instant.ofEpochSecond(date));
                return true;
            } else {
                //System.out.println("Текущее время " + new Date(date) + " меньше времени обновления уровня: " + new Date(timeUpgrade));
                setQuantity(building, simpleCalculateUpdate(building, resource, date));
                building.setLastUpdate(Instant.ofEpochSecond(date));
                return false;
            }
        }
    }

    private double simpleCalculateUpdate(BuildingP building, ResourceP resource, long date) {
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
        double addResources = ((date - building.getLastUpdate().getEpochSecond()) *
                (building.getQuantityMining() * Math.pow(building.getIncrementMining(), building.getLevel()))) * building.calculateIncrementMining();
        //System.out.println("добавляется " + addResources + " " + resource.getName());
        return addResources;
    }

    private void setQuantity(BuildingP building, double addResources) {
        double quantity = building.getResourcesInBuilding() + addResources;
        building.setResourcesInBuilding(
                building.calculateStorageLimit() < quantity ? quantity : building.calculateStorageLimit()
                );
        //System.out.println("теперь ресурсов: " + resource.getQuantity());
    }

    public double getQuantityInHour(BuildingP building) {
        if (building.getLevel() == 0) {
            return 0;
        }
        return 3_600 *
                (building.getQuantityMining() * Math.pow(building.getIncrementMining(), building.getLevel()));
    }

    public void addResourceOrIncrement(List<ResourceP> resources, ResourceType resourceType) {
        boolean isResourceHere = false;
        for (ResourceP resource : resources) {
            if (resource.getName().equals(resourceType)) {
                resource.incrementQuantity();
                isResourceHere = true;
            }
        }
        if (!isResourceHere) {
            resources.add(new ReferralBox1(1));
        }
    }

    public Set<ResourceP> findAllByUserId(long id) {
        return resourcesRepository.findAllByUserId(id);
    }
    public void save(List<ResourceP> resources) {
        resourcesRepository.saveAll(resources);
    }
    public void saveAll(Set<ResourceP> resources, long telegramId) {
        userCacheService.deleteFullUser(telegramId);
        resourcesRepository.saveAll(resources);
    }
    public void delete(ResourceP resource, long telegramId) {
        userCacheService.deleteFullUser(telegramId);
        resourcesRepository.delete(resource);
    }

    public List<ResourceP> getResources(Long id, Long telegramId) {
        if (resourceCacheService.isResourcesEmpty(telegramId)) {
            return new ArrayList<>();
        }
        List<ResourceP> resources = resourceCacheService.getResources(telegramId);
        if (!resources.isEmpty()) {
            return resources;
        }
        List<ResourceP> result = resourcesRepository.findAllByUserId(id).stream().toList();
        resourceCacheService.cacheResources(telegramId, result);
        return result;
    }

    public TransferResult sellResource(ResourceP resourceForSell, UserDto userDto) {
        List<ResourceP> userResources = userDto.getResources();
        if (resourceForSell.getName().equals(ResourceType.METAL)) {
            boolean isSubtract = trySubtractResource(userResources, resourceForSell);
            if (isSubtract) {
                ResourceP wantedResource = new Gold(resourceForSell.getQuantity() / 100 * 50);
                wantedResource.setUserId(userDto.getId());
                addResourceOrIncrement(userResources, wantedResource);
                saveResource(userResources, userDto.getTelegramId());
                return new TransferResult(null, resourceForSell, wantedResource);
            } else {
                return new TransferResult("Металла для продажи не хватает на складе." +
                        "\n Заберите ресурсы с карьера или попробуйте продать меньше.");
            }
        }
        List<BuildingP> buildings = userDto.getBuildings();


        System.out.println("пустой метод заглушка: sellResource: " + ResourceService.class);
        return null;
    }

    public TransferResult buyResource(ResourceP resource, UserDto userDto) {
        System.out.println("пустой метод заглушка: buyResource: " + ResourceService.class);
        return null;
    }

    // resourceToAdd уже должен быть с userId
    public void addResourceOrIncrement(List<ResourceP> userResources, ResourceP resourceToAdd) {
        boolean isResourceHere = false;
        for (ResourceP resource : userResources) {
            if (resource.getName().equals(resourceToAdd.getName())) {
                resource.addQuantity(resourceToAdd.getQuantity());
                isResourceHere = true;
                break;
            }
        }
        if (!isResourceHere) {
            userResources.add(resourceToAdd);
        }
    }

    public boolean trySubtractResource(List<ResourceP> userResources, ResourceP resourceToSubtract) {
        boolean isSubtract = false;
        for (ResourceP resource : userResources) {
            if (resource.getName().equals(resourceToSubtract.getName())) {
                if ((resource.getQuantity() - resourceToSubtract.getQuantity()) >= 0) {
                    resource.subtractQuantity(resourceToSubtract.getQuantity());
                    return true;
                }
                break;
            }
        }
        return isSubtract;
    }
}