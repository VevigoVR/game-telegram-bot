package com.creazione.space_learning.dto;

import com.creazione.space_learning.entities.game_entity.ResourceDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class WeightedResource {
    private ResourceDto resource;
    private double weight;
}