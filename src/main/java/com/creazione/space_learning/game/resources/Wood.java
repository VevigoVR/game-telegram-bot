package com.creazione.space_learning.game.resources;

import com.creazione.space_learning.entities.Resource;
import com.creazione.space_learning.enums.Emoji;
import com.creazione.space_learning.enums.ResourceType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("WOOD")
public class Wood extends Resource {
    public Wood() {
        super(ResourceType.WOOD, Emoji.WOOD);
    }

    public Wood(double quantity) {
        super(ResourceType.WOOD, Emoji.WOOD, quantity);
    }
}
