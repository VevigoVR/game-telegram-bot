package com.creazione.space_learning.game;

import com.creazione.space_learning.enums.ResourceType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Item {
    private ResourceType name;
    private double quantity;

    public void addQuantity(double quantity) {
        this.quantity += quantity;
    }
}