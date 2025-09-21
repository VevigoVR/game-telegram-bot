package com.creazione.space_learning.game;

import com.creazione.space_learning.enums.ResourceType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Item {
    private ResourceType name;
    private long quantity;

    public void addQuantity(long quantity) {
        this.quantity += quantity;
    }
}