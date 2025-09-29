package com.creazione.space_learning.game.resources;

import com.creazione.space_learning.entities.game_entity.ResourceDto;
import com.creazione.space_learning.enums.Emoji;
import com.creazione.space_learning.enums.ResourceType;

public class Coin extends ResourceDto {
    public Coin() {
        super(ResourceType.COIN, Emoji.NAZAR_AMULET);
    }

    public Coin(long quantity) {
        super(ResourceType.COIN, Emoji.NAZAR_AMULET, quantity);
    }

    public Coin(long id, long userId, long quantity) {
        super(ResourceType.COIN, Emoji.NAZAR_AMULET, id, userId, quantity);
    }
}
