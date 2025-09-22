package com.creazione.space_learning.game.resources;

import com.creazione.space_learning.entities.game_entity.ResourceDto;
import com.creazione.space_learning.enums.Emoji;
import com.creazione.space_learning.enums.ResourceType;

public class LootBoxCommon extends ResourceDto {
    public LootBoxCommon() {
        super(ResourceType.LOOT_BOX_COMMON, Emoji.PACKAGE);
    }

    public LootBoxCommon(long quantity) {
        super(ResourceType.LOOT_BOX_COMMON, Emoji.PACKAGE, quantity);
    }

    public LootBoxCommon(long id, long userId, long quantity) {
        super(ResourceType.LOOT_BOX_COMMON, Emoji.PACKAGE, id, userId, quantity);
    }
}