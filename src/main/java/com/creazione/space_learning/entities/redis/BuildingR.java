package com.creazione.space_learning.entities.redis;

import com.creazione.space_learning.entities.game_entity.BuildingDto;
import com.creazione.space_learning.enums.BuildingType;
import com.creazione.space_learning.enums.Emoji;
import com.creazione.space_learning.enums.ResourceType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(force = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BuildingR {
    private Long id;
    private Long userId;
    @JsonProperty("name")
    private BuildingType name;
    @JsonProperty("production")
    private ResourceType production;
    @JsonProperty("emoji")
    private Emoji emojiProduction;
    private double incrementPrice;
    private double resourcesInBuilding;
    private double incrementMining;
    private double quantityMining;
    private int level;
    private boolean isVisible = true;
    private long timeToUpdate;
    private Instant lastTimeUpgrade;
    private Instant lastUpdate;
}