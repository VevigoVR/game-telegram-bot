package com.creazione.space_learning.game.buildings;

import com.creazione.space_learning.entities.game_entity.BuildingDto;
import com.creazione.space_learning.entities.game_entity.ResourceDto;
import com.creazione.space_learning.enums.BuildingType;
import com.creazione.space_learning.enums.Emoji;
import com.creazione.space_learning.enums.ResourceType;
import com.creazione.space_learning.game.resources.Gold;
import com.creazione.space_learning.game.resources.Metal;
import com.creazione.space_learning.game.resources.Stone;

import java.util.ArrayList;
import java.util.List;

public class UnknownBuilding extends BuildingDto {
    public UnknownBuilding() {
        super(BuildingType.UNKNOWN, ResourceType.UNKNOWN, Emoji.UNKNOWN);
        this.setIncrementPrice(2);
        this.setIncrementMining(0);
        this.setQuantityMining(0);
        this.setVisible(false);
    }

    @Override
    public List<ResourceDto> viewPrice(int level) {
        List<ResourceDto> price = new ArrayList<>();
        int goldQuantity = 100;
        int stoneQuantity = 100;
        int metalQuantity = 100;
        if (level < 5) {
            price.add(new Gold(Math.round(goldQuantity * Math.pow(getIncrementPrice(), level))));
            price.add(new Stone(Math.round(stoneQuantity * Math.pow(getIncrementPrice(), level))));
        } else {
            price.add(new Stone(Math.round(stoneQuantity * Math.pow(getIncrementPrice(), level))));
            price.add(new Metal(Math.round(metalQuantity * Math.pow(getIncrementPrice(), level))));
        }
        return price;
    }
}
