package com.creazione.space_learning.game.resources;

import com.creazione.space_learning.entities.game_entity.ResourceDto;
import com.creazione.space_learning.enums.Emoji;
import com.creazione.space_learning.enums.ResourceType;

public class Crypto extends ResourceDto {
    public Crypto() {
        super(ResourceType.CRYPTO, Emoji.MONEY);
    }

    public Crypto(long quantity) {
        super(ResourceType.CRYPTO, Emoji.MONEY, quantity);
    }

    public Crypto(long id, long userId, long quantity) {
        super(ResourceType.CRYPTO, Emoji.MONEY, id, userId, quantity);
    }
}