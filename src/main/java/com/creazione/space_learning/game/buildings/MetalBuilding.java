package com.creazione.space_learning.game.buildings;

import com.creazione.space_learning.config.DataSet;
import com.creazione.space_learning.entities.game_entity.BuildingDto;
import com.creazione.space_learning.entities.game_entity.ResourceDto;
import com.creazione.space_learning.enums.BuildingType;
import com.creazione.space_learning.enums.ResourceType;
import com.creazione.space_learning.game.resources.*;
import com.creazione.space_learning.enums.Emoji;
import com.creazione.space_learning.utils.Formatting;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class MetalBuilding extends BuildingDto {
    public MetalBuilding() {
        super(BuildingType.METAL_BUILDING, ResourceType.METAL, Emoji.BLACK_CIRCLE);
        this.setIncrementPrice(2);
        this.setIncrementMining(1.5);
        this.setQuantityMining(0.005);
    }

    @Override
    public List<ResourceDto> viewPrice(int level) {
        List<ResourceDto> price = new ArrayList<>();
        long goldQuantity = 5;
        long stoneQuantity = 3;
        long metalQuantity = 5;
        if (level == 1) { // 1 уровень
            price.add(new Gold(goldQuantity));
        } else if (level > 1 && level < 5) { // 2,3,4 уровни
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
