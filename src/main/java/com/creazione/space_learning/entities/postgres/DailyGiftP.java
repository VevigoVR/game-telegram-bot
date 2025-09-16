package com.creazione.space_learning.entities.postgres;

import jakarta.persistence.Entity;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Getter
@Setter
@Table(name = "daily_gift", indexes = {
        @Index(name = "idx_gift_user_id", columnList = "user_id"),
        @Index(name = "idx_gift_calculated_at", columnList = "calculatedAt")
})

@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "@class"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = DailyGiftP.class, name = "dailyGift")
})
@NoArgsConstructor
public class DailyGiftP {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id")
    private long userId;
    @Column(name = "number_of_times")
    private int numberOfTimes;
    private Instant calculatedAt;
    @CreationTimestamp
    @Column(name = "reg_at")
    private Instant regAt;

    public DailyGiftP(long userId) {
        this.userId = userId;
        this.calculatedAt = Instant.now();
    }

    public void addNumberOfTimes() {
        this.numberOfTimes += 1;
    }
}
