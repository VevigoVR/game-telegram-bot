package com.creazione.space_learning.dto;

import com.creazione.space_learning.entities.game_entity.BuildingDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBuildingDto {
    private BuildingDto targetBuilding;
    private BuildingDto userBuilding;
    private boolean hasBuilding = false;
    private int iBuilding = 0;
}
