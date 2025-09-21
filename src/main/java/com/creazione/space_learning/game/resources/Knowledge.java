package com.creazione.space_learning.game.resources;

import com.creazione.space_learning.entities.postgres.ResourceP;
import com.creazione.space_learning.enums.Emoji;
import com.creazione.space_learning.enums.ResourceType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("KNOWLEDGE")
public class Knowledge extends ResourceP {
    public Knowledge() {
        super(ResourceType.KNOWLEDGE, Emoji.SPARKLE);
    }

    public Knowledge(long quantity) {
        super(ResourceType.KNOWLEDGE, Emoji.SPARKLE);
        this.setQuantity(quantity);
    }
}