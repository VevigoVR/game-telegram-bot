package com.creazione.space_learning.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@ToString
@Table(name = "ai_training_data")
public class AITrainingDataEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private String userMessage;
    private String context; // игровой контекст
    private OffsetDateTime timestamp;
}
