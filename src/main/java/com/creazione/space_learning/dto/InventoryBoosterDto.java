package com.creazione.space_learning.dto;

import com.creazione.space_learning.enums.ResourceType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryBoosterDto {
    private Long id;
    private Long userId;
    private ResourceType name;
    private Double value;
    private Long durationMilli;
    private double quantity;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InventoryBoosterDto that = (InventoryBoosterDto) o;
        return Double.compare(that.quantity, quantity) == 0 &&
                name == that.name &&
                Objects.equals(value, that.value) &&
                Objects.equals(durationMilli, that.durationMilli);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value, durationMilli, quantity);
    }
}