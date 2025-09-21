package com.creazione.space_learning.game.resources;

import com.creazione.space_learning.entities.postgres.ResourceP;
import com.creazione.space_learning.enums.Emoji;
import com.creazione.space_learning.enums.ResourceType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("COIN")
public class Coin extends ResourceP {
    public Coin() {
        super(ResourceType.COIN, Emoji.NAZAR_AMULET);
    }
    public Coin(long quantity) {
        super(ResourceType.COIN, Emoji.NAZAR_AMULET, quantity);
    }
}
