package com.creazione.space_learning.game.resources;

import com.creazione.space_learning.entities.postgres.ResourceP;
import com.creazione.space_learning.enums.Emoji;
import com.creazione.space_learning.enums.ResourceType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("GOLD")
public class Gold extends ResourceP {
    public Gold() {
        super(ResourceType.GOLD, Emoji.FULL_MOON);
    }

    public Gold(double quantity) {
        super(ResourceType.GOLD, Emoji.FULL_MOON, quantity);
    }
}
