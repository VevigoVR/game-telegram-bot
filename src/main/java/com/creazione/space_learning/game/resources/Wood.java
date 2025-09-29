package com.creazione.space_learning.game.resources;

import com.creazione.space_learning.entities.game_entity.ResourceDto;
import com.creazione.space_learning.enums.Emoji;
import com.creazione.space_learning.enums.ResourceType;

public class Wood extends ResourceDto {
    public Wood() {
        super(ResourceType.WOOD, Emoji.WOOD);
    }

    public Wood(long quantity) {
        super(ResourceType.WOOD, Emoji.WOOD, quantity);
    }

    public Wood(long id, long userId, long quantity) {
        super(ResourceType.WOOD, Emoji.WOOD, id, userId, quantity);
    }
}
