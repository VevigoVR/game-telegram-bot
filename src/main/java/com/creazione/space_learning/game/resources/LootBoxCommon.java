package com.creazione.space_learning.game.resources;

import com.creazione.space_learning.entities.postgres.ResourceP;
import com.creazione.space_learning.enums.Emoji;
import com.creazione.space_learning.enums.ResourceType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("LOOT_BOX_COMMON")
public class LootBoxCommon extends ResourceP {
    public LootBoxCommon() {
        super(ResourceType.LOOT_BOX_COMMON, Emoji.PACKAGE);
    }

    public LootBoxCommon(double quantity) {
        super(ResourceType.LOOT_BOX_COMMON, Emoji.PACKAGE);
        this.setQuantity(quantity);
    }
}