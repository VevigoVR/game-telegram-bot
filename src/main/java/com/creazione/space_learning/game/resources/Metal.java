package com.creazione.space_learning.game.resources;

import com.creazione.space_learning.entities.game_entity.ResourceDto;
import com.creazione.space_learning.enums.Emoji;
import com.creazione.space_learning.enums.ResourceType;

public class Metal extends ResourceDto {
    public Metal() {
        super(ResourceType.METAL, Emoji.BLACK_CIRCLE);
    }

    public Metal(long quantity) {
        super(ResourceType.METAL, Emoji.BLACK_CIRCLE, quantity);
    }

    public Metal(long id, long userId, long quantity) {
        super(ResourceType.METAL, Emoji.BLACK_CIRCLE, id, userId, quantity);
    }
}