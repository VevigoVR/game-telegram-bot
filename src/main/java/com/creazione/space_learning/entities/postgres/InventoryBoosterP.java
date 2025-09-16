package com.creazione.space_learning.entities.postgres;

import com.creazione.space_learning.game.Item;
import com.creazione.space_learning.enums.ResourceType;
import com.creazione.space_learning.utils.Formatting;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "inventory_boosters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "inventoryBoosterType")
public class InventoryBoosterP extends Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId; // Владелец

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Enumerated(EnumType.STRING)
    private ResourceType name; // Тип буста (SPEED)
    private Double value;     // Значение (0.5)
    private Long durationMilli; // Длительность (120)
    private double quantity; // Количество штук (1000)

    public InventoryBoosterP(ResourceType name, Double value, Long durationMilli, double quantity) {
        super();
        this.name = name;
        this.value = value;
        this.durationMilli = durationMilli;
        this.quantity = quantity;
    }

    @Override
    public void addQuantity(double quantity) {
        this.quantity += quantity;
    }

    public String makeQuantityString() {
        //DecimalFormat df = new DecimalFormat("0.#");
        //return df.format(quantity);
        return Formatting.formatWithDots(this.quantity);
    }
}