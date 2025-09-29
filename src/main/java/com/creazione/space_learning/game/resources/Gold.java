package com.creazione.space_learning.game.resources;

import com.creazione.space_learning.entities.game_entity.ResourceDto;
import com.creazione.space_learning.enums.Emoji;
import com.creazione.space_learning.enums.ResourceType;

public class Gold extends ResourceDto {
    public Gold() {
        super(ResourceType.GOLD, Emoji.FULL_MOON);
    }

    public Gold(long quantity) {
        super(ResourceType.GOLD, Emoji.FULL_MOON, quantity);
    }

    public Gold(long id, long userId, long quantity) {
        super(ResourceType.GOLD, Emoji.FULL_MOON, id, userId, quantity);
    }
}
