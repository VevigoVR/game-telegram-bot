package com.creazione.space_learning.entities.redis;

import com.creazione.space_learning.enums.SchedulerType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SchedulerR {
    private Long id;
    private SchedulerType type;
    private boolean run;
    private Long lastDuration;
    private Long previousDuration;
    private Instant lastStart;
    private Instant previousStart;
    private Instant lastEnd;
    private Instant previousEnd;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SchedulerR that = (SchedulerR) o;
        return run == that.run &&
                Objects.equals(id, that.id) &&
                type == that.type &&
                Objects.equals(lastDuration, that.lastDuration) &&
                Objects.equals(previousDuration, that.previousDuration) &&
                Objects.equals(lastStart, that.lastStart) &&
                Objects.equals(previousStart, that.previousStart) &&
                Objects.equals(lastEnd, that.lastEnd) &&
                Objects.equals(previousEnd, that.previousEnd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, run, lastDuration, previousDuration, lastStart, previousStart, lastEnd, previousEnd);
    }
}