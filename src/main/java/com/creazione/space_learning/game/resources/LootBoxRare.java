package com.creazione.space_learning.game.resources;

import com.creazione.space_learning.entities.game_entity.ResourceDto;
import com.creazione.space_learning.enums.Emoji;
import com.creazione.space_learning.enums.ResourceType;

public class LootBoxRare extends ResourceDto {
    public LootBoxRare() {
        super(ResourceType.LOOT_BOX_RARE, Emoji.CARD_FILE_BOX);
    }

    public LootBoxRare(long quantity) {
        super(ResourceType.LOOT_BOX_RARE, Emoji.CARD_FILE_BOX, quantity);
    }

    public LootBoxRare(long id, long userId, long quantity) {
        super(ResourceType.LOOT_BOX_RARE, Emoji.CARD_FILE_BOX, id, userId, quantity);
    }
}