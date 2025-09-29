package com.creazione.space_learning.game.buildings;

import com.creazione.space_learning.entities.game_entity.BuildingDto;

import java.util.List;

public class BuildingList {
    public static final List<BuildingDto> BUILDING_LIST = List.of(
            // УДАЛЯЕМ ЗОЛОТУЮ ШАХТУ И ЛЕСОПИЛКУ
            //new GoldBuilding(),
            new StoneBuilding(),
            // new WoodBuilding(),
            new MetalBuilding(),
            new StorageBuilding()
    );

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (BuildingDto building : BUILDING_LIST) {
            stringBuilder.append(building);
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }
}
