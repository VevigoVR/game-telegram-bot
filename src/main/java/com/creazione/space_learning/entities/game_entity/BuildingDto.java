package com.creazione.space_learning.entities.game_entity;

import com.creazione.space_learning.config.DataSet;
import com.creazione.space_learning.enums.BuildingType;
import com.creazione.space_learning.enums.Emoji;
import com.creazione.space_learning.enums.ResourceType;
import com.creazione.space_learning.utils.Formatting;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class BuildingDto {
    private Long id;
    private Long userId;
    private final BuildingType name;
    private final ResourceType production;
    private final Emoji emojiProduction;
    private double incrementPrice;
    private double resourcesInBuilding;
    private double incrementMining;
    private double quantityMining;
    private int level;
    private boolean isVisible = true;
    private long timeToUpdate;
    private Instant lastTimeUpgrade;
    private Instant lastUpdate;

    public BuildingDto(BuildingType name, ResourceType production, Emoji emoji) {
        this.name = name;
        this.production = production;
        this.level = 0;
        this.emojiProduction = emoji;
        this.lastUpdate = Instant.now();
        this.lastTimeUpgrade = Instant.now();
    }

    public BuildingDto() {
        this.production = ResourceType.UNKNOWN;
        this.name = BuildingType.UNKNOWN;
        this.emojiProduction = Emoji.UNKNOWN;
    }

    public BuildingDto(long id, BuildingType name, ResourceType production, Emoji emoji) {
        this.id = id;
        this.name = name;
        this.production = production;
        this.emojiProduction = emoji;
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
            sum += resource.getQuantity();
        }
        return sum;
    }

    public double calculateIncrementMining() { return 0.1; }

    public long calculateStorageLimit() {
        // возвращаем значение, равное 24 часам производства
        if (this.getLevel() == 0) {
            return 0;
        }
        //System.out.println(3_600 * (this.getQuantityMining() * Math.pow(this.getIncrementMining(), this.getLevel())));
        double quantityInHour = DataSet.getResourceService().getQuantityInHour(this) * 24;
        return Formatting.roundNumber((long) quantityInHour);
    }
}
