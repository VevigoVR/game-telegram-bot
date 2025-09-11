package com.creazione.space_learning.entities.channel_entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "awards", indexes = {
        @Index(name = "idx_user_id", columnList = "userId")
})
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class PointAward {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "userId", nullable = false)
    private Long userId;
    @Column(name = "points", nullable = false)
    private Long points;
    private String comment;
    private Date date;

    public PointAward(Long userId, long points) {
        this.userId = userId;
        this.points = points;
        this.comment = "";
        this.date = new Date();
    }

    public PointAward(Long userId, long points, String comment) {
        this.userId = userId;
        this.points = points;
        this.comment = comment;
        this.date = new Date();
    }
}
