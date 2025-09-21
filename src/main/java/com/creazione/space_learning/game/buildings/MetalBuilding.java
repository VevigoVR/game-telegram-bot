package com.creazione.space_learning.game.buildings;

import com.creazione.space_learning.config.DataSet;
import com.creazione.space_learning.entities.postgres.BuildingP;
import com.creazione.space_learning.entities.postgres.ResourceP;
import com.creazione.space_learning.enums.BuildingType;
import com.creazione.space_learning.enums.ResourceType;
import com.creazione.space_learning.game.resources.*;
import com.creazione.space_learning.enums.Emoji;
import com.creazione.space_learning.utils.Formatting;
import jakarta.persistence.Entity;

import java.util.ArrayList;
import java.util.List;

@Entity
public class MetalBuilding extends BuildingP {
    public MetalBuilding() {
        super(BuildingType.METAL_BUILDING, ResourceType.METAL);
        this.setIncrementPrice(2);
        this.setIncrementMining(1.25);
        this.setQuantityMining(0.005);
        this.setEmojiProduction(Emoji.BLACK_CIRCLE);
    }

    @Override
    public List<ResourceP> viewPrice(int level) {
        List<ResourceP> price = new ArrayList<>();
        int goldQuantity = 5;
        int stoneQuantity = 3;
        int metalQuantity = 5;
        if (level == 1) { // 1 уровень
            price.add(new Gold(goldQuantity));
        } else if (level > 1 && level < 5) { // 2,3,4 уровни
            System.out.println("level: " + level);
            System.out.println("getIncrementPrice(): " + getIncrementPrice());
            System.out.println("Math.pow(getIncrementPrice(), level): " + Math.pow(getIncrementPrice(), level));
            System.out.println("Math.round(metalQuantity * Math.pow(getIncrementPrice(), level)): " + Math.round(metalQuantity * Math.pow(getIncrementPrice(), level)));
            System.out.println("Formatting.roundNumber(Math.round(metalQuantity * Math.pow(getIncrementPrice(), level))): " + Formatting.roundNumber(Math.round(metalQuantity * Math.pow(getIncrementPrice(), level))));
            price.add(new Metal(Formatting.roundNumber(Math.round(metalQuantity * Math.pow(getIncrementPrice(), level)))));
        } else if (level > 4 && level < 10) { // 5,6,7,8,9 уровни
            price.add(new Gold(Formatting.roundNumber(Math.round(goldQuantity * Math.pow(getIncrementPrice(), level)))));
            price.add(new Metal(Formatting.roundNumber(Math.round(metalQuantity * Math.pow(getIncrementPrice(), level)))));
        } else {
            price.add(new Gold(Formatting.roundNumber(Math.round(goldQuantity * Math.pow(getIncrementPrice(), level)))));
            price.add(new Metal(Formatting.roundNumber(Math.round(metalQuantity * Math.pow(getIncrementPrice(), level)))));
            price.add(new Stone(Formatting.roundNumber(Math.round(stoneQuantity * Math.pow(getIncrementPrice(), level)))));
        }
        return price;
    }

    public double calculateIncrementMining() {
        double result = DataSet.getBoosterService().getIncrementMining(this, getLastUpdate());
        return result;
    }
}
