package com.creazione.space_learning.game.buildings;

import com.creazione.space_learning.config.DataSet;
import com.creazione.space_learning.entities.game_entity.ResourceDto;
import com.creazione.space_learning.entities.postgres.BuildingP;
import com.creazione.space_learning.enums.BuildingType;
import com.creazione.space_learning.enums.ResourceType;
import com.creazione.space_learning.game.resources.*;
import com.creazione.space_learning.enums.Emoji;
import com.creazione.space_learning.utils.Formatting;
import jakarta.persistence.Entity;

import java.util.ArrayList;
import java.util.List;

@Entity
public class StoneBuilding extends BuildingP {
    public StoneBuilding() {
        super(BuildingType.STONE_BUILDING, ResourceType.STONE);
        this.setIncrementPrice(2);
        this.setIncrementMining(1.25);
        this.setQuantityMining(0.01);
        this.setEmojiProduction(Emoji.ROCK);
    }

    @Override
    public List<ResourceDto> viewPrice(int level) {
        List<ResourceDto> price = new ArrayList<>();
        int goldQuantity = 5;
        int stoneQuantity = 7;
        int metalQuantity = 5;
        if (level == 1) { // 1 уровень
            price.add(new Gold(goldQuantity));
        } else if (level > 1 && level < 5) { // 2,3,4 уровни
            price.add(new Stone(Formatting.roundNumber(Math.round(stoneQuantity * Math.pow(getIncrementPrice(), level)))));
        } else if (level > 4 && level < 10) { // 5,6,7,8,9 уровни
            price.add(new Gold(Formatting.roundNumber(Math.round(goldQuantity * Math.pow(getIncrementPrice(), level)))));
            price.add(new Stone(Formatting.roundNumber(Math.round(stoneQuantity * Math.pow(getIncrementPrice(), level)))));
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
