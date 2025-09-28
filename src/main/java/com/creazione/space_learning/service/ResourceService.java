package com.creazione.space_learning.service;

import com.creazione.space_learning.dto.TransferBuildingResult;
import com.creazione.space_learning.dto.TransferTradeResult;
import com.creazione.space_learning.entities.game_entity.ResourceDto;
import com.creazione.space_learning.entities.game_entity.UserDto;
import com.creazione.space_learning.entities.postgres.BuildingP;
import com.creazione.space_learning.game.resources.Gold;
import com.creazione.space_learning.game.resources.ReferralBox1;
import com.creazione.space_learning.game.resources.ResourceList;
import com.creazione.space_learning.enums.ResourceType;
import com.creazione.space_learning.service.postgres.ResourcePostgresService;
import com.creazione.space_learning.utils.Formatting;
import com.creazione.space_learning.utils.WordUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final ResourcePostgresService resourcePostgresService;


    public boolean calculateQuantityChanges(UserDto userDto, Instant date) {
        List<ResourceDto> resources = userDto.getResources();
        List<BuildingP> buildings = userDto.getBuildings();
        long dateLong = date.getEpochSecond();
        boolean isUpdateDB = false;
        //System.out.println("Текущее время: " + new Date(dateLong));
        for (BuildingP building : buildings) {
            boolean flag = false;
            for (ResourceDto resource : resources) {
                if (resource.getName().equals(building.getProduction())) {
                    //System.out.println("Ресурс существует: " + resource.getName() + " - " + resource.getQuantity() + "шт.");
                    isUpdateDB = calculateUpdate(building, resource, dateLong);
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                for (ResourceDto resource : ResourceList.RESOURCES_LIST) {
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

    private boolean calculateUpdate(BuildingP building, ResourceDto resource, long date) {
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

    private double simpleCalculateUpdate(BuildingP building, ResourceDto resource, long date) {
        if (building.getLevel() == 0) {
            return 0;
        }
        // calculateIncrementMining() - это бонус или ограничение от бустеров
        double addResources = ((date - building.getLastUpdate().getEpochSecond()) *
                (building.getQuantityMining() * Math.pow(building.getIncrementMining(), building.getLevel()))) * building.calculateIncrementMining();
        //System.out.println("добавляется " + addResources + " " + resource.getName());
        return addResources;
    }

    private void setQuantity(BuildingP building, double addResources) {
        double quantity = building.getResourcesInBuilding() + addResources;
        building.setResourcesInBuilding(
                building.calculateStorageLimit() > quantity ? quantity : building.calculateStorageLimit()
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

    public void addResourceRefBoxOrIncrement(List<ResourceDto> resources, ResourceType resourceType) {
        boolean isResourceHere = false;
        for (ResourceDto resource : resources) {
            if (resource.getName().equals(resourceType)) {
                resource.incrementQuantity();
                isResourceHere = true;
            }
        }
        if (!isResourceHere) {
            resources.add(new ReferralBox1(1));
        }
    }

    public TransferTradeResult sellResource(ResourceDto resourceForSell, UserDto userDto) {
        List<ResourceDto> userResources = userDto.getResources();
        TransferTradeResult transferResult = new TransferTradeResult();
        for (ResourceDto userResource : userResources) {
            if ((userResource.getName().getName().equals(resourceForSell.getName().getName()))
                    && (userResource.getQuantity() >= resourceForSell.getQuantity())
            ) {
                if (resourceForSell.getName().getName().equals(ResourceType.METAL.getName())) {

                    Gold gold = new Gold((resourceForSell.getQuantity() / 100 * 75));
                    //System.out.println("Количество золота: " + gold.getQuantity());
                    gold.setUserId(userDto.getId());
                    addResourceOrIncrement(userResources, gold);
                    userResource.setQuantity(userResource.getQuantity() - resourceForSell.getQuantity());
                    transferResult.setUserResource(resourceForSell);
                    transferResult.setNpcResource(gold);
                    transferResult.setBuy(false);
                    transferResult.setTransferred(true);
                    resourcePostgresService.saveAll(userResources, userDto.getTelegramId());
                    return transferResult;
                } else if (resourceForSell.getName().getName().equals(ResourceType.STONE.getName())) {
                    Gold gold = new Gold((resourceForSell.getQuantity() / 100 * 50));
                    gold.setUserId(userDto.getId());
                    addResourceOrIncrement(userResources, gold);
                    userResource.subtractQuantity(resourceForSell.getQuantity());
                    transferResult.setUserResource(resourceForSell);
                    transferResult.setNpcResource(gold);
                    transferResult.setBuy(false);
                    transferResult.setTransferred(true);
                    resourcePostgresService.saveAll(userResources, userDto.getTelegramId());
                    return transferResult;
                } else {
                    return new TransferTradeResult("Этот ресурс не продаётся!");
                }
            }
        }
        return new TransferTradeResult("Для продажи не хватает ресурсов!");
    }

    public TransferTradeResult buyResource(ResourceDto resourceForBuy, UserDto userDto) {
        List<ResourceDto> userResources = userDto.getResources();
        TransferTradeResult transferResult = new TransferTradeResult();
        transferResult.setNpcResource(resourceForBuy);
        transferResult.setBuy(true);
        if (resourceForBuy.getName().getName().equals(ResourceType.GOLD.getName())) {
            TransferTradeResult result =  new TransferTradeResult("Золото можно получить только при продаже ресурсов...");
            result.setTransferred(false);
            return result;
        }
        long needGold = 0L;
        for (ResourceDto userResource : userResources) {
            if (userResource.getName().getName().equals(ResourceType.GOLD.getName())) {
                transferResult.setUserResource(userResource);
                break;
            }
        }

        switch (resourceForBuy.getName()) {
            case METAL -> {
                needGold = resourceForBuy.getQuantity() / 100 * 80;
            }
            case STONE -> {
                needGold = resourceForBuy.getQuantity() / 100 * 55;
            }
        }

        if (transferResult.getUserResource() == null) {
            TransferTradeResult result =  new TransferTradeResult("Для покупки " + resourceForBuy.getName().getName() + " в размере "
                    + Formatting.formatWithDots(resourceForBuy.getQuantity()) + " шт. не хватает "
                    + Formatting.formatWithDots(needGold) + " "
                    + WordUtils.rightWord(needGold, "золото", "золота", "золота"));
            result.setTransferred(false);
            return result;
        } else if (transferResult.getUserResource().getQuantity() < needGold) {

            TransferTradeResult result =  new TransferTradeResult("Для покупки " + resourceForBuy.getName().getName() + " в размере "
                    + Formatting.formatWithDots(resourceForBuy.getQuantity()) + " шт. не хватает "
                    + Formatting.formatWithDots(needGold - transferResult.getUserResource().getQuantity()) + " "
                    + WordUtils.rightWord((needGold - transferResult.getUserResource().getQuantity()), "золото", "золота", "золота"));
            result.setTransferred(false);
            return result;
        } else {
            resourceForBuy.setUserId(userDto.getId());
            addResourceOrIncrement(userResources, resourceForBuy);
            for (ResourceDto userResource : userResources) {
                if (userResource.getName().getName().equals(ResourceType.GOLD.getName())) {
                    //System.out.println(userResource.getQuantity());
                    userResource.subtractQuantity(needGold);
                    //System.out.println(userResource.getQuantity());
                    transferResult.setUserResource(new Gold(needGold));
                    transferResult.setTransferred(true);
                    break;
                }
            }
            resourcePostgresService.saveAll(userResources, userDto.getTelegramId());
            return transferResult;
        }
    }

    // resourceToAdd уже должен быть с userId
    public void addResourceOrIncrement(List<ResourceDto> userResources, ResourceDto resourceToAdd) {
        boolean isResourceHere = false;
        for (ResourceDto resource : userResources) {
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

    public boolean trySubtractResource(List<ResourceDto> userResources, ResourceDto resourceToSubtract) {
        boolean isSubtract = false;
        for (ResourceDto resource : userResources) {
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

    public TransferBuildingResult calculateTransfer(double accumulatedResources) {
        // Проверяем, что накопилось хотя бы 1 единица
        if (accumulatedResources < 1.0) {
            return new TransferBuildingResult(0L, accumulatedResources);
        }

        // Выделяем целую часть
        long wholePart = (long) Math.floor(accumulatedResources);

        // Вычисляем остаток
        double remainder = accumulatedResources - wholePart;

        // Убеждаемся, что остаток не отрицательный и корректен
        remainder = Math.max(0.0, remainder);
        remainder = Math.min(remainder, 0.9999999999); // Не больше 1.0

        return new TransferBuildingResult(wholePart, remainder);
    }
}