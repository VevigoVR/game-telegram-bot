package com.creazione.space_learning.game.resources;

import com.creazione.space_learning.entities.game_entity.ResourceDto;
import com.creazione.space_learning.enums.Emoji;
import com.creazione.space_learning.enums.ResourceType;

public class Unknown extends ResourceDto {
    public Unknown() {
        super(ResourceType.UNKNOWN, Emoji.ARROW_RIGHT);
    }

    public Unknown(long quantity) {
        super(ResourceType.UNKNOWN, Emoji.ARROW_RIGHT, quantity);
    }

    public Unknown(long id, long userId, long quantity) {
        super(ResourceType.UNKNOWN, Emoji.ARROW_RIGHT, id, userId, quantity);
    }
}
