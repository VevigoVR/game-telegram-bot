package com.creazione.space_learning.service;

import com.creazione.space_learning.config.DataSet;
import com.creazione.space_learning.entities.game_entity.BuildingDto;
import com.creazione.space_learning.entities.game_entity.ResourceDto;
import com.creazione.space_learning.entities.game_entity.UserDto;
import com.creazione.space_learning.game.buildings.*;
import com.creazione.space_learning.repository.BuildingRepository;
import com.creazione.space_learning.enums.Emoji;
import com.creazione.space_learning.service.postgres.UserPostgresService;
import com.creazione.space_learning.service.redis.BuildingCacheService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BuildingService {
    private final UserPostgresService userService;
    private final BuildingCacheService buildingCacheService;
    private final BuildingRepository buildingRepository;

    @Transactional
    public String createBuilding(UserDto user, BuildingDto newBuilding) {
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
    private String create(UserDto user, BuildingDto newBuilding) {
        // Производит вычисление, не сохраняет
        DataSet.getResourceService().calculateQuantityChanges(user, Instant.now());

        //System.out.println("Класс строения: " + newBuilding.getClass());
        List<ResourceDto> needResources = newBuilding.viewPrice(1);
        List<ResourceDto> userResources = user.getResources();

        if (!checkResources(needResources, userResources)) {
            return "\n" + Emoji.EXCLAMATION + "<b>На строительство не хватает ресурсов</b>\n\n";
        }
        newBuilding.setTimeToUpdate(getDuration(needResources));
        newBuilding.setUserId(user.getId());

        user.getBuildings().add(newBuilding);
        userService.saveFull(user);
        return "\n" + Emoji.STAR2 + "<b>Строительство успешно начато!</b>\n\n";
    }

    public String upLevel(UserDto user, int iBuilding) {
        Instant date = Instant.now();

        BuildingDto building = user.getBuildings().get(iBuilding);
        List<ResourceDto> needResources = building.viewPrice(building.getLevel() + 1);
        List<ResourceDto> userResources = user.getResources();


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
        userService.saveFull(user);
        return "\n" + Emoji.STAR2 + "<b>Строительство успешно начато!</b>\n\n";
    }

    private boolean checkTime (BuildingDto building) {
        Instant date = Instant.now();
        long expireDate = date.getEpochSecond() - (building.getLastTimeUpgrade().getEpochSecond() + building.getTimeToUpdate());
        return expireDate >= 0;
    }

    private boolean checkResources(List<ResourceDto> needResources, List<ResourceDto> userResources) {
        for (ResourceDto needResource : needResources) {
            //System.out.println("needResource: " + needResource.getName() + " - " + needResource.getQuantity());
            boolean isHere = false;
            for (ResourceDto userResource : userResources) {
                if (!needResource.getName().equals(userResource.getName())) {
                    continue;
                }
                if (needResource.getQuantity() > userResource.getQuantity()) {
                    return false;
                } else {
                    isHere = true;
                    long quantity = userResource.getQuantity();
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

    public long getDuration(List<ResourceDto> resources) {
        long sum = 0;
        for (ResourceDto resource : resources) {
            sum += resource.getQuantity();
        }
        sum /= 10L;
        return sum;
    }

    public String getDurationToString(long duration) {
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

    public BuildingDto cloneBuilding(BuildingDto building) {
        BuildingDto clone = new BuildingDto(building.getId(), building.getName(), building.getProduction(), building.getEmojiProduction());
        clone.setUserId(building.getUserId());
        clone.setIncrementPrice(building.getIncrementPrice());
        clone.setIncrementMining(building.getIncrementMining());
        clone.setQuantityMining(building.getQuantityMining());
        clone.setLevel(building.getLevel());
        clone.setTimeToUpdate(building.getTimeToUpdate());
        clone.setLastTimeUpgrade(building.getLastTimeUpgrade());
        clone.setLastUpdate(building.getLastUpdate());
        return clone;
    }

    public long getPointsForAllBuildings(List<BuildingDto> buildings)  {
        long sum = 0;
        if (buildings == null) { return 0; }
        for (BuildingDto building : buildings) {
            sum += building.getPointsForBuilding(building.getLevel());
        }
        return sum/1000;
    }
}
