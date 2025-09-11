package com.creazione.space_learning.entities;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "player_scores", indexes = {
        @Index(name = "idx_score_user_id", columnList = "user_id"),
        @Index(name = "idx_score_calculated_at", columnList = "calculatedAt")
})


@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "@class"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PlayerScore.class, name = "playerScore")
})
@NoArgsConstructor
public class PlayerScore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id")
    private long userId;
    private long score;
    private Instant calculatedAt;

    public PlayerScore(long userId) {
        this.userId = userId;
        this.score = 0;
        this.calculatedAt = Instant.now();
    }
}