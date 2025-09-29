package com.creazione.space_learning.game.buildings;

import com.creazione.space_learning.config.DataSet;
import com.creazione.space_learning.entities.game_entity.BuildingDto;
import com.creazione.space_learning.entities.game_entity.ResourceDto;
import com.creazione.space_learning.enums.BuildingType;
import com.creazione.space_learning.enums.ResourceType;
import com.creazione.space_learning.game.resources.*;
import com.creazione.space_learning.enums.Emoji;

import java.util.ArrayList;
import java.util.List;

public class GoldBuilding extends BuildingDto {
    public GoldBuilding() {
        super(BuildingType.GOLD_BUILDING, ResourceType.GOLD, Emoji.FULL_MOON);
        this.setIncrementPrice(2);
        this.setIncrementMining(1.25);
        this.setQuantityMining(0.01);
    }

    @Override
    public List<ResourceDto> viewPrice(int level) {
        List<ResourceDto> price = new ArrayList<>();
        long goldQuantity = 5;
        //long woodQuantity = 7;
        long stoneQuantity = 5;
        long metalQuantity = 7;
        if (level == 1) {
            price.add(new Gold(goldQuantity));
        } else if (level < 20 && level > 0) {
            price.add(new Gold(Math.round(goldQuantity * Math.pow(getIncrementPrice(), level))));
            price.add(new Stone(Math.round(stoneQuantity * Math.pow(getIncrementPrice(), level))));
            price.add(new Metal(Math.round(metalQuantity * Math.pow(getIncrementPrice(), level)) * 2));
        } else if (level > 19){
            price.add(new Gold(Math.round(goldQuantity * Math.pow(getIncrementPrice(), level))));
            price.add(new Stone(Math.round(stoneQuantity * Math.pow(getIncrementPrice(), level))));
            price.add(new Metal(Math.round(metalQuantity * Math.pow(getIncrementPrice(), level)) * 2));
        }
        return price;
    }

    public double calculateIncrementMining() {
        double result = DataSet.getBoosterService().getIncrementMining(this, getLastUpdate());
        return result;
    }
}
