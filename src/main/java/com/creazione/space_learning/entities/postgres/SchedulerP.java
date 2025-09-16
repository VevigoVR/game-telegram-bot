package com.creazione.space_learning.entities.postgres;

import com.creazione.space_learning.enums.SchedulerType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
/**
 * Объект временной метки, который постоянно вызывается при каждом запросе, чтобы узнать,
 * можно ли начислить пользователю ресурсы за старые нотификации с подарками
 */
@Entity
@Table(name = "scheduler")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SchedulerP {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private SchedulerType type;

    private boolean run;
    private Long lastDuration;
    private Long previousDuration;
    private Instant lastStart;
    private Instant previousStart;
    private Instant lastEnd;
    private Instant previousEnd;
}