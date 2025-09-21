package com.creazione.space_learning.game.resources;

import com.creazione.space_learning.entities.postgres.ResourceP;
import com.creazione.space_learning.enums.Emoji;
import com.creazione.space_learning.enums.ResourceType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("WOOD")
public class Wood extends ResourceP {
    public Wood() {
        super(ResourceType.WOOD, Emoji.WOOD);
    }

    public Wood(long quantity) {
        super(ResourceType.WOOD, Emoji.WOOD, quantity);
    }
}
