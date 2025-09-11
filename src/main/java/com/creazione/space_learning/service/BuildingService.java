package com.creazione.space_learning.service;

import com.creazione.space_learning.config.DataSet;
import com.creazione.space_learning.dto.UserDto;
import com.creazione.space_learning.entities.Building;
import com.creazione.space_learning.entities.UserEntity;
import com.creazione.space_learning.game.buildings.*;
import com.creazione.space_learning.entities.Resource;
import com.creazione.space_learning.repository.BuildingRepository;
import com.creazione.space_learning.enums.Emoji;
import com.creazione.space_learning.service.redis.BuildingCacheService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BuildingService {
    private final UserService userService;
    private final BuildingCacheService buildingCacheService;
    private final BuildingRepository buildingRepository;

    @Transactional
    public String createBuilding(UserDto user, Building newBuilding) {
        switch (newBuilding.getName()) {
            case GOLD_BUILDING -> {
                return create(user, new GoldBuilding());
            }
            case WOOD_BUILDING -> {
                return create(user, new WoodBuilding());
            }
            case STONE_BUILDING -> {
                return create(user, new StoneBuilding());
            }
            case METAL_BUILDING -> {
                return create(user, new MetalBuilding());
            }
        }
        return "";
    }

    @Transactional
    private String create(UserDto user, Building newBuilding) {
        // Производит вычисление, не сохраняет
        DataSet.getResourceService().calculateQuantityChanges(user, Instant.now());

        //System.out.println("Класс строения: " + newBuilding.getClass());
        List<Resource> needResources = newBuilding.viewPrice(1);
        List<Resource> userResources = user.getResources();

        if (!checkResources(needResources, userResources)) {
            return "\n" + Emoji.EXCLAMATION + "<b>На строительство не хватает ресурсов</b>\n\n";
        }
        newBuilding.setTimeToUpdate(getDuration(needResources));
        UserEntity userEntity = user.convertToUserEntity();
        newBuilding.setUserId(userEntity.getId());

        userEntity.getBuildings().add(newBuilding);
        userService.saveFull(userEntity);
        return "\n" + Emoji.STAR2 + "<b>Строительство успешно начато!</b>\n\n";
    }

    public String upLevel(UserDto user, int iBuilding) {
        Instant date = Instant.now();
        // Производит вычисление, не сохраняет
        DataSet.getResourceService().calculateQuantityChanges(user, date);

        Building building = user.getBuildings().get(iBuilding);
        List<Resource> needResources = building.viewPrice(building.getLevel() + 1);
        List<Resource> userResources = user.getResources();


        if (!checkTime(building)) {
            return "\n" + Emoji.EXCLAMATION + "<b>Время строительства не закончено</b>\n\n";
        }

        if (!checkResources(needResources, userResources)) {
            return "\n" + Emoji.EXCLAMATION + "<b>На строительство не хватает ресурсов</b>\n\n";
        }

        building.setLastUpdate(date); // последнее обновление ресурсов
        building.setLastTimeUpgrade(date); // время с последнего улучшения уровня строения
        building.setTimeToUpdate(getDuration(needResources)); // время, которое требуется для улучшения уровня строения
        //building.setUser(user);
        userService.saveFull(user.convertToUserEntity());
        return "\n" + Emoji.STAR2 + "<b>Строительство успешно начато!</b>\n\n";
    }

    private boolean checkTime (Building building) {
        Date date = new Date();
        long expireDate = date.getTime() - (building.getLastTimeUpgrade().toEpochMilli() + building.getTimeToUpdate());
        return expireDate >= 0;
    }

    private boolean checkResources(List<Resource> needResources, List<Resource> userResources) {
        for (Resource needResource : needResources) {
            //System.out.println("needResource: " + needResource.getName() + " - " + needResource.getQuantity());
            boolean isHere = false;
            for (Resource userResource : userResources) {
                if (!needResource.getName().equals(userResource.getName())) {
                    continue;
                }
                if (needResource.getQuantity() > userResource.getQuantity()) {
                    return false;
                } else {
                    isHere = true;
                    double quantity = userResource.getQuantity();
                    quantity -= needResource.getQuantity();
                    userResource.setQuantity(quantity);
                }
            }
            if (!isHere) {
                return false;
            }
        }
        return true;
    }

    public long getDuration(List<Resource> resources) {
        long sum = 0;
        for (Resource resource : resources) {
            sum += (long) resource.getQuantity();
        }
        sum *= (long) 100.0; // необходимо преобразование в миллисекунды для метода, сохраняющего время для улучшения уровня строения
        return sum;
    }

    public String getDurationToString(long duration) {
        duration = duration / 1000;
        if (duration < 1) {
            return " 0с";
        }
        StringBuilder result = new StringBuilder();

        long day = duration / (24 * 3600);
        if (day > 0) {
            result.append(" ").append(day).append("д");
        }

        duration = duration % (24 * 3600);
        long hour = duration / 3600;
        if (hour > 0) {
            result.append(" ").append(hour).append("ч");
        }

        duration %= 3600;
        long minutes = duration / 60 ;
        if (minutes > 0) {
            result.append(" ").append(minutes).append("м");
        }

        duration %= 60;
        long seconds = duration;
        if (seconds > 0) {
            result.append(" ").append(seconds).append("с");
        }

        return result.toString();
    }

    public Building cloneBuilding(Building building) {
        Building clone = new Building(building.getId(), building.getName(), building.getProduction());
        clone.setUserId(building.getUserId());
        clone.setEmojiProduction(building.getEmojiProduction());
        clone.setIncrementPrice(building.getIncrementPrice());
        clone.setIncrementMining(building.getIncrementMining());
        clone.setQuantityMining(building.getQuantityMining());
        clone.setLevel(building.getLevel());
        clone.setTimeToUpdate(building.getTimeToUpdate());
        clone.setLastTimeUpgrade(building.getLastTimeUpgrade());
        clone.setLastUpdate(building.getLastUpdate());
        return clone;
    }

    public long getPointsForAllBuildings(List<Building> buildings)  {
        long sum = 0;
        for (Building building : buildings) {
            sum += building.getPointsForBuilding(building.getLevel());
        }
        return sum/1000;
    }

    public List<Building> getBuildings(Long id, Long telegramId) {
        if (buildingCacheService.isBuildingsEmpty(telegramId)) {
            return new ArrayList<>();
        }
        List<Building> buildings = buildingCacheService.getBuildings(telegramId);
        if (!buildings.isEmpty()) {
            return buildings;
        }
        List<Building> result = buildingRepository.findAllByUserId(id).stream().toList();
        buildingCacheService.cacheBuildings(telegramId, result);
        return result;
    }
}
