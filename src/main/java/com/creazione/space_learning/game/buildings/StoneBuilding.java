package com.creazione.space_learning.game.buildings;

import com.creazione.space_learning.config.DataSet;
import com.creazione.space_learning.entities.postgres.BuildingP;
import com.creazione.space_learning.entities.postgres.ResourceP;
import com.creazione.space_learning.enums.BuildingType;
import com.creazione.space_learning.enums.ResourceType;
import com.creazione.space_learning.game.resources.*;
import com.creazione.space_learning.enums.Emoji;
import jakarta.persistence.Entity;

import java.util.ArrayList;
import java.util.List;

@Entity
public class StoneBuilding extends BuildingP {
    public StoneBuilding() {
        super(BuildingType.STONE_BUILDING, ResourceType.STONE);
        this.setIncrementPrice(2);
        this.setIncrementMining(1.25);
        this.setQuantityMining(0.0001);
        this.setEmojiProduction(Emoji.ROCK);
    }

    @Override
    public List<ResourceP> viewPrice(int level) {
        List<ResourceP> price = new ArrayList<>();
        int goldQuantity = 7;
        int woodQuantity = 5;
        int stoneQuantity = 7;
        int metalQuantity = 5;
        if (level == 1) {
            price.add(new Gold(goldQuantity));
        } else if (level < 20) {
            price.add(new Gold(Math.round(goldQuantity * Math.pow(getIncrementPrice(), level))));
            price.add(new Wood(Math.round(woodQuantity * Math.pow(getIncrementPrice(), level))));
            price.add(new Stone(Math.round(stoneQuantity * Math.pow(getIncrementPrice(), level))));
        } else {
            price.add(new Gold(Math.round(goldQuantity * Math.pow(getIncrementPrice(), level))));
            price.add(new Wood(Math.round(woodQuantity * Math.pow(getIncrementPrice(), level))));
            price.add(new Stone(Math.round(stoneQuantity * Math.pow(getIncrementPrice(), level))));
        }
        return price;
    }

    public double calculateIncrementMining() {
        double result = DataSet.getBoosterService().getIncrementMining(this, getLastUpdate());
        return result;
    }
}
