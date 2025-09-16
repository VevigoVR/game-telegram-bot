package com.creazione.space_learning.entities.redis;

import com.creazione.space_learning.entities.postgres.NoticeP;
import com.creazione.space_learning.entities.postgres.PlayerScoreP;
import com.creazione.space_learning.entities.postgres.UserP;
import com.creazione.space_learning.entities.postgres.InventoryBoosterP;
import com.creazione.space_learning.entities.postgres.BuildingP;
import com.creazione.space_learning.entities.postgres.ResourceP;
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
public class UserR {
    @JsonProperty("id")
    private Long id;
    @JsonProperty("telegramId")
    private Long telegramId;
    @JsonProperty("name")
    private String name;
    @JsonProperty("buildings")
    private List<BuildingP> buildings = new ArrayList<>();
    @JsonProperty("resources")
    private List<ResourceP> resources = new ArrayList<>();
    @JsonProperty("boosters")
    private List<InventoryBoosterP> boosters = new ArrayList<>();
    @JsonProperty("player_score")
    private PlayerScoreP playerScore;
    private Long referrer;
    private Integer totalReferrals = 0;
    @JsonProperty("notices")
    private List<NoticeP> notices = new ArrayList<>();
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
}