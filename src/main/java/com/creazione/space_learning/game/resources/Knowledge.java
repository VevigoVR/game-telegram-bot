package com.creazione.space_learning.game.resources;

import com.creazione.space_learning.entities.game_entity.ResourceDto;
import com.creazione.space_learning.enums.Emoji;
import com.creazione.space_learning.enums.ResourceType;

public class Knowledge extends ResourceDto {
    public Knowledge() {
        super(ResourceType.KNOWLEDGE, Emoji.SPARKLE);
    }

    public Knowledge(long quantity) {
        super(ResourceType.KNOWLEDGE, Emoji.SPARKLE, quantity);
    }

    public Knowledge(long id, long userId, long quantity) {
        super(ResourceType.KNOWLEDGE, Emoji.SPARKLE, id, userId, quantity);
    }
}