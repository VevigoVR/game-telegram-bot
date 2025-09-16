package com.creazione.space_learning.game.buildings;

import com.creazione.space_learning.entities.postgres.BuildingP;

import java.util.List;

public class BuildingList {
    public static final List<BuildingP> BUILDING_LIST = List.of(
            // УДАЛЯЕМ ЗОЛОТУЮ ШАХТУ
            //new GoldBuilding(),
            new StoneBuilding(),
            new WoodBuilding(),
            new MetalBuilding(),
            new StorageBuilding()
    );

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (BuildingP building : BUILDING_LIST) {
            stringBuilder.append(building);
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }
}
