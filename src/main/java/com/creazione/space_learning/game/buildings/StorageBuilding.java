package com.creazione.space_learning.game.buildings;

import com.creazione.space_learning.entities.Building;
import com.creazione.space_learning.entities.Resource;
import com.creazione.space_learning.enums.BuildingType;
import com.creazione.space_learning.enums.ResourceType;
import com.creazione.space_learning.game.resources.*;
import com.creazione.space_learning.enums.Emoji;

import java.util.ArrayList;
import java.util.List;

public class StorageBuilding extends Building {
    public StorageBuilding() {
        super(BuildingType.STORAGE_BUILDING, ResourceType.UNKNOWN);
        this.setIncrementPrice(2);
        this.setIncrementMining(0);
        this.setQuantityMining(0);
        this.setEmojiProduction(Emoji.DEPARTMENT_STORE);
        this.setVisible(false);
    }

    @Override
    public List<Resource> viewPrice(int level) {
        List<Resource> price = new ArrayList<>();
        int goldQuantity = 100;
        int woodQuantity = 100;
        int stoneQuantity = 100;
        int metalQuantity = 100;
        if (level < 5) {
            price.add(new Gold(Math.round(goldQuantity * Math.pow(getIncrementPrice(), level))));
            price.add(new Wood(Math.round(woodQuantity * Math.pow(getIncrementPrice(), level))));
            price.add(new Stone(Math.round(stoneQuantity * Math.pow(getIncrementPrice(), level))));
        } else {
            price.add(new Wood(Math.round(woodQuantity * Math.pow(getIncrementPrice(), level))));
            price.add(new Stone(Math.round(stoneQuantity * Math.pow(getIncrementPrice(), level))));
            price.add(new Metal(Math.round(metalQuantity * Math.pow(getIncrementPrice(), level))));
        }
        return price;
    }
}
