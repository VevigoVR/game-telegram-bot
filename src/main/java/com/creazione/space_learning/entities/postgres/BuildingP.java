package com.creazione.space_learning.entities.postgres;

import com.creazione.space_learning.entities.game_entity.ResourceDto;
import com.creazione.space_learning.enums.BuildingType;
import com.creazione.space_learning.enums.ResourceType;
import com.creazione.space_learning.game.buildings.*;
import com.creazione.space_learning.enums.Emoji;
import com.creazione.space_learning.utils.Formatting;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Getter
@Setter
@Entity
@AllArgsConstructor
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
    @Transient
    private Emoji emojiProduction;
    @Transient
    private double incrementPrice;
    private double resourcesInBuilding;
    private double incrementMining;
    private double quantityMining;
    private int level;
    private boolean isVisible = true;
    private long timeToUpdate;
    private Instant lastTimeUpgrade;
    private Instant lastUpdate;

    public BuildingP(BuildingType name, ResourceType production) {
        this.name = name;
        this.production = production;
        this.level = 0;
        this.lastUpdate = Instant.now();
        this.lastTimeUpgrade = Instant.now();
    }

    public BuildingP() {
        this.production = ResourceType.UNKNOWN;
        this.name = BuildingType.UNKNOWN;
    }

    public BuildingP(long id, BuildingType name, ResourceType production) {
        this.id = id;
        this.name = name;
        this.production = production;
    }

    @Override
    public String toString() {
        return  this.getName() + ": " + this.level + " уровень, производство: " + this.getProduction();
    }

    public void upLevel() {
        this.level += 1;
    }

    public List<ResourceDto> viewPrice(int level) {
        return new ArrayList<>();
    }

    public long getPointsForBuilding(int level) {
        long sum = 0;
        for (int i = 1; i <= level; i++) {
            sum += getPointsForLevel(i);
        }
        return sum;
    }

    public long getPointsForLevel(int level) {
        List<ResourceDto> resources = viewPrice(level);
        long sum = 0;
        for (ResourceDto resource : resources) {
            sum += (long) resource.getQuantity();
        }
        return sum;
    }

    public double calculateIncrementMining() { return 0.1; }

    public long calculateStorageLimit() {
        // возвращаем значение, равное 24 часам производства
        if (this.getLevel() == 0) {
            return 0;
        }
        long quantityInHour = 3_600 *
                (long) (this.getQuantityMining() * Math.pow(this.getIncrementMining(), this.getLevel()));

        return Formatting.roundNumber(quantityInHour * 24);
    }
}