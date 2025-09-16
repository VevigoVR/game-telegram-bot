package com.creazione.space_learning.game.resources;

import com.creazione.space_learning.entities.postgres.ResourceP;
import com.creazione.space_learning.enums.Emoji;
import com.creazione.space_learning.enums.ResourceType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("UNKNOWN")
public class Unknown extends ResourceP {
    public Unknown() {
        super(ResourceType.UNKNOWN, Emoji.ARROW_RIGHT);
    }
    public Unknown(double quantity) {
        super(ResourceType.UNKNOWN, Emoji.ARROW_RIGHT, quantity);
    }
}
