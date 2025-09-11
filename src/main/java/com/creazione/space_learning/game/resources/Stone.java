package com.creazione.space_learning.game.resources;

import com.creazione.space_learning.entities.Resource;
import com.creazione.space_learning.enums.Emoji;
import com.creazione.space_learning.enums.ResourceType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("STONE")
public class Stone extends Resource {
    public Stone() {
        super(ResourceType.STONE, Emoji.ROCK);
    }

    public Stone(double quantity) {
        super(ResourceType.STONE, Emoji.ROCK);
        this.setQuantity(quantity);
    }
}
