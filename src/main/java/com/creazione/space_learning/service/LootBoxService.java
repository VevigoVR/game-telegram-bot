package com.creazione.space_learning.service;

import com.creazione.space_learning.config.DataSet;
import com.creazione.space_learning.dto.UserDto;
import com.creazione.space_learning.entities.redis.UserR;
import com.creazione.space_learning.entities.postgres.ResourceP;
import com.creazione.space_learning.enums.ResourceType;
import com.creazione.space_learning.game.Item;
import com.creazione.space_learning.entities.postgres.InventoryBoosterP;
import com.creazione.space_learning.repository.InventoryBoosterRepository;
import com.creazione.space_learning.entities.postgres.BuildingP;
import com.creazione.space_learning.game.resources.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;

@Service
@RequiredArgsConstructor
public class LootBoxService {
    private final InventoryBoosterRepository inventoryBoosterRepository;
    private final Random random = new Random();
    private UserDto userDto = new UserDto();
    private ResourceType boxType;
    private Set<ResourceP> resources;
    private long milliSecondsGift;

    public List<Item> openBox(ResourceType boxType, UserDto userDto) {
        this.boxType = boxType;
        this.userDto = userDto;
        this.resources = DataSet.getResourceService().findAllByUserId(userDto.getId());
        if (!decrementResources(resources)) {
            //System.out.println("получился быстрый false на проверке сундучка");
            return null;
        }
        List<Item> possibleRewards = getPossibleRewards(boxType);
        return save(selectRandomReward(possibleRewards, 1));
    }

    public List<Item> takeDailyGift(UserDto userDto) {
        this.userDto = userDto;
        this.resources = DataSet.getResourceService().findAllByUserId(userDto.getId());
        List<Item> possibleRewards = getPossibleRewards(ResourceType.LOOT_BOX_COMMON);
        return save(selectRandomReward(possibleRewards, 1));
    }

    private List<Item> getPossibleRewards(ResourceType boxType) {

        switch (boxType.getMark()) {
            case "common":
                milliSecondsGift = 7_200_000;
                return Arrays.asList(
                        new Gold(15),
                        new Wood(15),
                        new Stone(15),
                        new Metal(15),
                        new Gold(15),
                        new Wood(15),
                        new Stone(15),
                        new Metal(15),
                        new Gold(15),
                        new Wood(15),
                        new Stone(15),
                        new Metal(15),
                        new Gold(15),
                        new Wood(15),
                        new Stone(15),
                        new Metal(15),
                        //new Knowledge(1),
                        new InventoryBoosterP(
                                ResourceType.ACCELERATION_METAL,
                                0.2, Duration.ofHours(24).toMillis(), 1),
                        new InventoryBoosterP(
                                ResourceType.ACCELERATION_STONE,
                                0.2, Duration.ofHours(24).toMillis(), 1),
                        new InventoryBoosterP(
                                ResourceType.ACCELERATION_WOOD,
                                0.2, Duration.ofHours(24).toMillis(), 1),
                        new InventoryBoosterP(
                                ResourceType.ACCELERATION_ALL,
                                0.2, Duration.ofHours(24).toMillis(), 1)
                );
            case "rare":
                milliSecondsGift = 18_000_000;
                return Arrays.asList(
                        new Gold(50),
                        new Wood(50),
                        new Stone(50),
                        new Metal(50),
                        new InventoryBoosterP(
                                ResourceType.ACCELERATION_ALL,
                                0.3, Duration.ofHours(24).toMillis(), 1)
                        //new Knowledge(5)
                );

            case "ref 1":
                milliSecondsGift = 18_000_000;
                return Arrays.asList(
                        new Gold(50),
                        new InventoryBoosterP(
                                ResourceType.ACCELERATION_ALL,
                                0.5, Duration.ofHours(24).toMillis(), 1),
                        new Wood(50),
                        new Metal(50),
                        new Stone(50)
                        //new Knowledge(10)
                );
            case "ref 2":
                milliSecondsGift = 36_000_000;
                return Arrays.asList(
                        new Gold(150),
                        new InventoryBoosterP(
                                ResourceType.ACCELERATION_ALL,
                                1.0, Duration.ofHours(24).toMillis(), 1),
                        new Wood(150),
                        new Metal(150),
                        new Stone(150)
                        //new Knowledge(25)
                );
            case "ref 3":
                milliSecondsGift = 72_000_000;
                return Arrays.asList(
                        new Gold(2000),
                        new InventoryBoosterP(
                                ResourceType.ACCELERATION_ALL,
                                1.5, Duration.ofHours(24).toMillis(), 1),
                        new Wood(2000),
                        new Metal(2000),
                        new Stone(2000)
                        //new Knowledge(50)
                );
            // ... другие типы лутбоксов
            default:
                return Collections.emptyList();
        }
    }

    private double incrementQuantityGiftByLevel(ResourceType type, double quantity) {
        for (BuildingP building : userDto.getBuildings()) {
            if (building.getProduction().equals(type)) {
                return Math.round (((building.getQuantityMining() * Math.pow(building.getIncrementMining(), building.getLevel()))) * milliSecondsGift);
            }
        }
        return quantity;
    }

    private List<Item> selectRandomReward(List<Item> rewards, int count) {
        if (rewards.isEmpty()) {
            return new ArrayList<>();
        }
        List<Item> items = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            // Простая реализация случайного выбора
            items.add(rewards.get(random.nextInt(rewards.size())));
        }
        for (Item item : items) {
            if (!(item instanceof ResourceP)) {
                continue;
            }
            item.setQuantity(incrementQuantityGiftByLevel(item.getName(), item.getQuantity()));
        }
        return items;
    }

    private List<Item> save(List<Item> items) {
        List<ResourceP> giftResources = new ArrayList<>();
        List<InventoryBoosterP> giftBoosters = new ArrayList<>();
        for (Item item : items) {
            if (item instanceof ResourceP) {
                giftResources.add((ResourceP) item);
            } else {
                giftBoosters.add((InventoryBoosterP) item);
            }
        }

        if (!giftResources.isEmpty()) {
            addOrIncrementResource(resources, giftResources, userDto.getId()); // верно getId()
            DataSet.getResourceService().saveAll(resources, userDto.getTelegramId()); // верно getTelegramId()
        }
        if (!giftBoosters.isEmpty()) {
            Set<InventoryBoosterP> boosters = DataSet.getBoosterService().findAllIBByUserId(userDto);
            addOrIncrementInventoryBoosters(boosters, giftBoosters, userDto.getId()); // верно getId()
            DataSet.getBoosterService().saveAllIB(boosters, userDto.getTelegramId()); // верно getTelegramId()
        }
        return items;
    }

    public void addOrIncrementResource(Set<ResourceP> userResources, List<ResourceP> grantedResources, Long userId) {

        for (ResourceP grant : grantedResources) {
            boolean found = false;
            for (ResourceP userResource : userResources) {
                if (userResource.getName().equals(grant.getName())) {
                    userResource.addQuantity(grant.getQuantity());
                    found = true;
                    break;
                }
            }
            if (!found) {
                // Создаем новый ресурс правильного типа
                grant.setUserId(userId);
                grant.setQuantity(grant.getQuantity());
                userResources.add(grant);
            }
        }
    }

    public boolean decrementResources(Set<ResourceP> resources) {
        for (ResourceP resource : resources) {
            //System.out.println("resources for decrement :" + resource.getName());
            if (resource.getName().equals(boxType)) {
                if (resource.getQuantity() <= 0) {
                    DataSet.getResourceService().delete(resource, userDto.getTelegramId());
                    //System.out.println("result of decrement: " + false);
                    return false;
                }
                resource.setQuantity(resource.getQuantity() - 1);
                //System.out.println("result of decrement: " + true);
                return true;
            }
        }
        return false;
    }

    public static void addOrIncrementInventoryBoosters(Set<InventoryBoosterP> userResources, List<InventoryBoosterP> grantedResources, Long userId) {
        for (InventoryBoosterP grant : grantedResources) {
            boolean found = false;
            for (InventoryBoosterP userResource : userResources) {
                if (userResource.getName().equals(grant.getName())
                        && userResource.getValue().equals(grant.getValue())
                        && userResource.getDurationMilli().equals(grant.getDurationMilli())) {
                    userResource.addQuantity(grant.getQuantity());
                    found = true;
                    break;
                }
            }
            if (!found) {
                // Создаем новый ресурс правильного типа
                grant.setUserId(userId);
                userResources.add(grant);
            }
        }
    }
}