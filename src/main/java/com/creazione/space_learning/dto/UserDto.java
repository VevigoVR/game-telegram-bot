package com.creazione.space_learning.dto;

import com.creazione.space_learning.entities.NoticeEntity;
import com.creazione.space_learning.entities.PlayerScore;
import com.creazione.space_learning.entities.UserEntity;
import com.creazione.space_learning.entities.InventoryBooster;
import com.creazione.space_learning.entities.Building;
import com.creazione.space_learning.entities.Resource;
import com.creazione.space_learning.enums.ResourceType;
import com.creazione.space_learning.utils.InventoryBoosterSorter;
import com.creazione.space_learning.utils.BuildingSorter;
import com.creazione.space_learning.utils.ResourceSorter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.*;

import java.time.Instant;
import java.util.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDto {
    @JsonProperty("id")
    private Long id;
    @JsonProperty("telegramId")
    private Long telegramId;
    @JsonProperty("name")
    private String name;
    @JsonProperty("buildings")
    private List<Building> buildings = new ArrayList<>();
    @JsonProperty("resources")
    private List<Resource> resources = new ArrayList<>();
    @JsonProperty("boosters")
    private List<InventoryBooster> boosters = new ArrayList<>();
    @JsonProperty("player_score")
    private PlayerScore playerScore;
    private Long referrer;
    private Integer totalReferrals = 0;
    @JsonProperty("notices")
    private Set<NoticeEntity> notices = new HashSet<>();
    private boolean isSuperAggregate;
    private boolean isPost;
    //@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant updatedAt;
    //@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public UserEntity convertToUserEntity() {
        return new UserEntity(this.getId(),
                this.getTelegramId(),
                this.getName(),
                new HashSet<>(this.getBuildings()),
                new HashSet<>(this.getResources()),
                new HashSet<>(this.getBoosters()),
                this.getPlayerScore(),
                this.getReferrer(),
                this.getTotalReferrals(),
                new HashSet<>(this.getNotices()),
                this.isSuperAggregate(),
                this.isPost(),
                this.getUpdatedAt(),
                this.getCreatedAt()
                );
    }

    public List<Resource> getResources() {
        if (this.resources == null) {
            return new ArrayList<>();
        }
        return this.resources;
    }

    public List<Building> getBuildings() {
        if (this.buildings == null) {
            return  new ArrayList<>();
        }
        return this.buildings;
    }

    public List<InventoryBooster> getBoosters() {
        if (this.boosters == null) {
            return new ArrayList<>();
        }
        return this.boosters;
    }

    public List<Resource> viewSortedResources() {
        if (resources == null) {
            return new ArrayList<>();
        } else {
            return ResourceSorter.sortResources(this.resources, ResourceSorter.SortOrder.NAME_ASC);
        }
    }

    public List<InventoryBooster> viewSortedBoosters() {
        if (boosters == null) {
            return new ArrayList<>();
        } else {
            return InventoryBoosterSorter.sortResources(this.boosters, InventoryBoosterSorter.SortOrder.NAME_ASC);
        }
    }

    public List<Building> viewSortedBuildings() {
        if (buildings == null) {
            return  new ArrayList<>();
        } else {
            return BuildingSorter.sortBuildings(this.buildings, BuildingSorter.SortOrder.NAME_ASC);
        }
    }

    public List<Resource> viewSortedLootBoxes() {
        List<ResourceType> lootBoxTypes = ResourceType.getLootBoxTypes();
        List<Resource> userLootBoxes = new ArrayList<>();
        for (Resource resource : resources) {
            if (lootBoxTypes.contains(resource.getName())) {
                userLootBoxes.add(resource);
            }
        }
        return ResourceSorter.sortResources(userLootBoxes, ResourceSorter.SortOrder.NAME_ASC);
    }

    public List<Resource> viewSortedCommon() {
        List<ResourceType> commonTypes = ResourceType.getCommonResourceTypes();
        List<Resource> userCommon = new ArrayList<>();
        for (Resource resource : resources) {
            if (commonTypes.contains(resource.getName())) {
                userCommon.add(resource);
            }
        }
        return ResourceSorter.sortResources(userCommon, ResourceSorter.SortOrder.NAME_ASC);
    }
}