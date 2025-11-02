package com.creazione.space_learning.service.postgres;

import com.creazione.space_learning.entities.game_entity.BuildingDto;
import com.creazione.space_learning.entities.postgres.BuildingP;
import com.creazione.space_learning.game.buildings.*;
import com.creazione.space_learning.repository.BuildingRepository;
import com.creazione.space_learning.service.redis.BuildingCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BuildingPostgresService {
    private final BuildingRepository buildingRepository;
    private final BuildingCacheService buildingCacheService;

    public List<BuildingP> toPostgresObjectList(List<BuildingDto> buildingDtoList) {
        if (buildingDtoList == null) {
            return new ArrayList<>();
        }
        List<BuildingP> buildingPList = new ArrayList<>();
        for (BuildingDto building : buildingDtoList) {
            buildingPList.add(toPostgresObject(building));
        }
        return buildingPList;
    }

    public List<BuildingDto> toGameObjectList(List<BuildingP> buildingPSet) {
        if (buildingPSet == null) {
            return new ArrayList<>();
        }
        List<BuildingDto> buildingDtoList = new ArrayList<>();
        for (BuildingP building : buildingPSet) {
            buildingDtoList.add(toGameObject(building));
        }
        return buildingDtoList;
    }

    private BuildingP toPostgresObject(BuildingDto buildingDto) {
        return new BuildingP(
                buildingDto.getId(),
                buildingDto.getUserId(),
                buildingDto.getName(),
                buildingDto.getProduction(),
                buildingDto.getEmojiProduction(),
                buildingDto.getIncrementPrice(),
                buildingDto.getResourcesInBuilding(),
                buildingDto.getIncrementMining(),
                buildingDto.getQuantityMining(),
                buildingDto.getLevel(),
                buildingDto.isVisible(),
                buildingDto.getTimeToUpdate(),
                buildingDto.getLastTimeUpgrade(),
                buildingDto.getLastUpdate()
        );
    }

    private BuildingDto toGameObject(BuildingP buildingP) {
        switch (buildingP.getName()) {
            case METAL_BUILDING -> {
                MetalBuilding metalBuilding = new MetalBuilding();
                convertNotFinalFieldToGameObject(metalBuilding, buildingP);
                return metalBuilding;
            }
            case GOLD_BUILDING -> {
                GoldBuilding goldBuilding = new GoldBuilding();
                convertNotFinalFieldToGameObject(goldBuilding, buildingP);
                return goldBuilding;
            }
            case STONE_BUILDING -> {
                StoneBuilding stoneBuilding = new StoneBuilding();
                convertNotFinalFieldToGameObject(stoneBuilding, buildingP);
                return stoneBuilding;
            }
            case WOOD_BUILDING -> {
                WoodBuilding woodBuilding = new WoodBuilding();
                convertNotFinalFieldToGameObject(woodBuilding, buildingP);
                return woodBuilding;
            }
            case STORAGE_BUILDING -> {
                StorageBuilding storageBuilding = new StorageBuilding();
                convertNotFinalFieldToGameObject(storageBuilding, buildingP);
                return storageBuilding;
            }
            case DATA_CENTRE -> {
                DataCentreBuilding dataCentreBuilding = new DataCentreBuilding();
                convertNotFinalFieldToGameObject(dataCentreBuilding, buildingP);
                return dataCentreBuilding;
            }
            default -> {
                UnknownBuilding unknownBuilding = new UnknownBuilding();
                convertNotFinalFieldToGameObject(unknownBuilding, buildingP);
                return unknownBuilding;
            }
        }
    }

    private void convertNotFinalFieldToGameObject(BuildingDto buildingDto, BuildingP buildingP) {
        buildingDto.setId(buildingP.getId());
        buildingDto.setUserId(buildingP.getUserId());
        buildingDto.setIncrementPrice(buildingP.getIncrementPrice());
        buildingDto.setResourcesInBuilding(buildingP.getResourcesInBuilding());
        buildingDto.setIncrementMining(buildingP.getIncrementMining());
        buildingDto.setQuantityMining(buildingP.getQuantityMining());
        buildingDto.setLevel(buildingP.getLevel());
        buildingDto.setVisible(buildingP.isVisible());
        buildingDto.setTimeToUpdate(buildingP.getTimeToUpdate());
        buildingDto.setLastTimeUpgrade(buildingP.getLastTimeUpgrade());
        buildingDto.setLastUpdate(buildingP.getLastUpdate());
    }

    public void saveAll(List<BuildingDto> buildings, long telegramId) {
        buildingCacheService.deleteBuildings(telegramId);
        buildingRepository.saveAll(toPostgresObjectList(buildings));
    }

    public List<BuildingDto> getBuildings(Long id, Long telegramId) {
        if (buildingCacheService.isBuildingsEmpty(telegramId)) {
            return new ArrayList<>();
        }
        List<BuildingDto> buildings = buildingCacheService.getBuildings(telegramId);
        if (!buildings.isEmpty()) {
            return buildings;
        }
        List<BuildingP> result = buildingRepository.findAllByUserId(id).stream().toList();
        buildingCacheService.cacheBuildings(telegramId, toGameObjectList(result));
        return toGameObjectList(result);
    }
}
