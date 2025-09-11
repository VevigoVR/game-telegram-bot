package com.creazione.space_learning.entities.channel_entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "user_channel", indexes = {
        @Index(name = "idx_user_tgchannel_id", columnList = "telegram_id"),
})
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UserChannel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "telegram_id", nullable = false)
    private Long telegramId;
    @OneToMany(mappedBy = "userId", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<PointAward> pointAward;
    @Column(name = "points", nullable = false)
    private Long points;
    private Date date;

    public UserChannel(Long telegramId) {
        this.telegramId = telegramId;
        this.points = 0L;
        this.pointAward = new ArrayList<>();
        this.date = new Date();
    }

    public UserChannel(Long telegramId, long points) {
        this.telegramId = telegramId;
        this.points = points;
        this.pointAward = new ArrayList<>();
        this.date = new Date();
    }

    public long addPoints(int points, String adminReaction) {
        return this.points += points;
    }
}
