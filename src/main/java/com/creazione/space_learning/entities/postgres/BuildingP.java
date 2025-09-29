package com.creazione.space_learning.entities.postgres;

import com.creazione.space_learning.enums.BuildingType;
import com.creazione.space_learning.enums.ResourceType;
import com.creazione.space_learning.game.buildings.*;
import com.creazione.space_learning.enums.Emoji;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;

@Slf4j
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "buildings", indexes = {
        @Index(name = "idx_building_user_id", columnList = "user_id"),
        @Index(name = "idx_building_last_update", columnList = "lastUpdate")
})
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "buildingType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = GoldBuilding.class, name = "GOLD_BUILDING"),
        @JsonSubTypes.Type(value = MetalBuilding.class, name = "METAL_BUILDING"),
        @JsonSubTypes.Type(value = StoneBuilding.class, name = "STONE_BUILDING"),
        @JsonSubTypes.Type(value = WoodBuilding.class, name = "WOOD_BUILDING")
})
@DiscriminatorColumn(name = "building_type", discriminatorType = DiscriminatorType.STRING)
public class BuildingP {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Enumerated(EnumType.STRING)
    private final BuildingType name;
    @Enumerated(EnumType.STRING)
    private final ResourceType production;
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