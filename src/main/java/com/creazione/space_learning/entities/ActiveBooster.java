package com.creazione.space_learning.entities;

import com.creazione.space_learning.enums.ResourceType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "activeBoosterType")
@Table(name = "active_boosters")
public class ActiveBooster {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId; // Владелец

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Enumerated(EnumType.STRING)
    private ResourceType name; // Тип бустера (SPEED_METAL)
    private Double value;     // Значение (0.5)
    private Instant endsAt;   // Когда закончит действие
    private Instant startsAt; // Когда активирован

    public ActiveBooster (Long userId, ResourceType name, double rate, int timeInHours) {
        this.userId = userId;
        this.name = name;
        this.value = rate;
        this.startsAt = Instant.now();
        this.endsAt = startsAt.plusSeconds(timeInHours * 3600L + 1L);
    }
}