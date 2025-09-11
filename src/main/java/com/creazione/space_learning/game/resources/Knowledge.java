package com.creazione.space_learning.game.resources;

import com.creazione.space_learning.entities.Resource;
import com.creazione.space_learning.enums.Emoji;
import com.creazione.space_learning.enums.ResourceType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("KNOWLEDGE")
public class Knowledge extends Resource {
    public Knowledge() {
        super(ResourceType.KNOWLEDGE, Emoji.SPARKLE);
    }

    public Knowledge(double quantity) {
        super(ResourceType.KNOWLEDGE, Emoji.SPARKLE);
        this.setQuantity(quantity);
    }
}