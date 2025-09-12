package com.creazione.space_learning.game.buildings;

import com.creazione.space_learning.config.DataSet;
import com.creazione.space_learning.entities.Building;
import com.creazione.space_learning.entities.Resource;
import com.creazione.space_learning.enums.BuildingType;
import com.creazione.space_learning.enums.ResourceType;
import com.creazione.space_learning.game.resources.*;
import com.creazione.space_learning.enums.Emoji;
import jakarta.persistence.Entity;

import java.util.ArrayList;
import java.util.List;

@Entity
public class MetalBuilding extends Building {
    public MetalBuilding() {
        super(BuildingType.METAL_BUILDING, ResourceType.METAL);
        this.setIncrementPrice(2);
        this.setIncrementMining(1.25);
        this.setQuantityMining(0.0001);
        this.setEmojiProduction(Emoji.BLACK_CIRCLE);
    }

    @Override
    public List<Resource> viewPrice(int level) {
        List<Resource> price = new ArrayList<>();
        int goldQuantity = 7;
        int woodQuantity = 5;
        int stoneQuantity = 6;
        int metalQuantity = 7;
        if (level == 1) {
            price.add(new Gold(goldQuantity));
        } else {
            price.add(new Wood(Math.round(woodQuantity * Math.pow(getIncrementPrice(), level))));
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
