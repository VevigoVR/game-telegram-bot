package com.creazione.space_learning.game.resources;

import com.creazione.space_learning.entities.Resource;
import com.creazione.space_learning.enums.Emoji;
import com.creazione.space_learning.enums.ResourceType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("COIN")
public class Coin extends Resource {
    public Coin() {
        super(ResourceType.COIN, Emoji.NAZAR_AMULET);
    }
    public Coin(double quantity) {
        super(ResourceType.COIN, Emoji.NAZAR_AMULET, quantity);
    }
}
