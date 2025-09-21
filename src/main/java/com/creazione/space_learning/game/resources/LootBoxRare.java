package com.creazione.space_learning.game.resources;

import com.creazione.space_learning.entities.postgres.ResourceP;
import com.creazione.space_learning.enums.Emoji;
import com.creazione.space_learning.enums.ResourceType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("LOOT_BOX_RARE")
public class LootBoxRare extends ResourceP {
    public LootBoxRare() {
        super(ResourceType.LOOT_BOX_RARE, Emoji.CARD_FILE_BOX);
    }

    public LootBoxRare(long quantity) {
        super(ResourceType.LOOT_BOX_RARE, Emoji.CARD_FILE_BOX);
        this.setQuantity(quantity);
    }
}