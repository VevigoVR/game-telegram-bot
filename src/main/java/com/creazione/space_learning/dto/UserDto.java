package com.creazione.space_learning.dto;

import com.creazione.space_learning.entities.postgres.*;
import com.creazione.space_learning.enums.ResourceType;
import com.creazione.space_learning.utils.BuildingSorter;
import com.creazione.space_learning.utils.InventoryBoosterSorter;
import com.creazione.space_learning.utils.ResourceSorter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long id;
    private Long telegramId;
    private String name;
    private List<BuildingP> buildings;
    private List<ResourceP> resources;
    private List<InventoryBoosterP> boosters;
    private PlayerScoreP playerScore;
    private Long referrer;
    private Integer totalReferrals = 0;
    private List<NoticeP> notices;
    private boolean isSuperAggregate;
    private boolean isPost;
    private Instant updatedAt;
    private Instant createdAt;

    public List<ResourceP> viewSortedResources() {
        if (resources == null) {
            return new ArrayList<>();
        } else {
            return ResourceSorter.sortResources(this.resources, ResourceSorter.SortOrder.NAME_ASC);
        }
    }

    public List<InventoryBoosterP> viewSortedBoosters() {
        if (boosters == null) {
            return new ArrayList<>();
        } else {
            return InventoryBoosterSorter.sortResources(this.boosters, InventoryBoosterSorter.SortOrder.NAME_ASC);
        }
    }

    public List<BuildingP> viewSortedBuildings() {
        if (buildings == null) {
            return  new ArrayList<>();
        } else {
            return BuildingSorter.sortBuildings(this.buildings, BuildingSorter.SortOrder.NAME_ASC);
        }
    }

    public List<ResourceP> viewSortedLootBoxes() {
        List<ResourceType> lootBoxTypes = ResourceType.getLootBoxTypes();
        List<ResourceP> userLootBoxes = new ArrayList<>();
        for (ResourceP resource : resources) {
            if (lootBoxTypes.contains(resource.getName())) {
                userLootBoxes.add(resource);
            }
        }
        return ResourceSorter.sortResources(userLootBoxes, ResourceSorter.SortOrder.NAME_ASC);
    }

    public List<ResourceP> viewSortedCommon() {
        List<ResourceType> commonTypes = ResourceType.getCommonResourceTypes();
        List<ResourceP> userCommon = new ArrayList<>();
        for (ResourceP resource : resources) {
            if (commonTypes.contains(resource.getName())) {
                userCommon.add(resource);
            }
        }
        return ResourceSorter.sortResources(userCommon, ResourceSorter.SortOrder.NAME_ASC);
    }

    public void incrementTotalReferrals() {
        totalReferrals++;
    }
}
