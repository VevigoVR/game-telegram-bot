package com.creazione.space_learning.game.resources;

import com.creazione.space_learning.entities.Resource;
import com.creazione.space_learning.enums.Emoji;
import com.creazione.space_learning.enums.ResourceType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("METAL")
public class Metal extends Resource {
    public Metal() {
        super(ResourceType.METAL, Emoji.BLACK_CIRCLE);
    }

    public Metal(double quantity) {
        super(ResourceType.METAL, Emoji.BLACK_CIRCLE);
        this.setQuantity(quantity);
    }
}