package com.creazione.space_learning.dto;

import com.creazione.space_learning.enums.ResourceType;
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
public class ActiveBoosterDto {
    private Long id;
    private Long userId;
    private ResourceType name;
    private Double value;
    private Instant endsAt;
    private Instant startsAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActiveBoosterDto that = (ActiveBoosterDto) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(value, that.value) &&
                Objects.equals(endsAt, that.endsAt) &&
                Objects.equals(startsAt, that.startsAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value, endsAt, startsAt);
    }
}