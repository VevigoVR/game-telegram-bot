package com.creazione.space_learning.entities.postgres;

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
public class UserP {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "telegramId", nullable = false)
    private Long telegramId;
    private String name;
    @OneToMany(mappedBy = "userId", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<BuildingP> buildings;
    @OneToMany(mappedBy = "userId", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<ResourceP> resources;
    @OneToMany(mappedBy = "userId", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<InventoryBoosterP> boosters;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "player_score_id") // Новый столбец в таблице users
    private PlayerScoreP playerScore;
    private Long referrer;
    private Integer totalReferrals = 0;
    @OneToMany(mappedBy = "userId", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<NoticeP> notices;
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
}