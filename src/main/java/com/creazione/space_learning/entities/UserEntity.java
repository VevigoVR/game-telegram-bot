package com.creazione.space_learning.entities;

import com.creazione.space_learning.dto.UserDto;
import com.creazione.space_learning.utils.BuildingSorter;
import com.creazione.space_learning.utils.ResourceSorter;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_telegram_id", columnList = "telegramId"),
        @Index(name = "idx_user_referrer", columnList = "referrer")
})
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "telegramId", nullable = false)
    private Long telegramId;
    private String name;
    @OneToMany(mappedBy = "userId", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Building> buildings;
    @OneToMany(mappedBy = "userId", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Resource> resources;
    @OneToMany(mappedBy = "userId", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<InventoryBooster> boosters;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "player_score_id") // Новый столбец в таблице users
    private PlayerScore playerScore;
    private Long referrer;
    private Integer totalReferrals = 0;
    @OneToMany(mappedBy = "userId", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<NoticeEntity> notices;
    private boolean isSuperAggregate;
    private boolean isPost;
    private Instant updatedAt;
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

// профайлер , бесплатный -Visual VM
    public UserDto convertToUserDto() {
        return new UserDto(this.getId(),
                this.getTelegramId(),
                this.getName(),
                new ArrayList<>(this.getBuildings()),
                new ArrayList<>(this.getResources()),
                new ArrayList<>(this.getBoosters()),
                this.getPlayerScore(),
                this.getReferrer(),
                this.getTotalReferrals(),
                new HashSet<>(this.getNotices()),
                this.isSuperAggregate(),
                this.isPost(),
                this.getUpdatedAt(),
                this.getCreatedAt());
    }

    public void incrementTotalReferrals() {
        totalReferrals++;
    }

    public Set<Building> viewSortedBuildings() {
        return BuildingSorter.sortBuildingsAsSet(this.buildings);
    }

    public Set<Resource> viewSortedResources() {
        return ResourceSorter.sortResourcesAsSet(this.resources);
    }
}