package com.creazione.space_learning.game.resources;

import com.creazione.space_learning.entities.game_entity.ResourceDto;
import com.creazione.space_learning.enums.Emoji;
import com.creazione.space_learning.enums.ResourceType;
import lombok.Getter;

@Getter
public class Stone extends ResourceDto {

    private final double buyForGold = 0.55;
    private final double sellForGold = 0.5;
    private final boolean forTrade = true;

    public Stone() {
        super(ResourceType.STONE, Emoji.ROCK);
    }

    public Stone(long quantity) {
        super(ResourceType.STONE, Emoji.ROCK, quantity);
    }

    public Stone(long id, long userId, long quantity) {
        super(ResourceType.STONE, Emoji.ROCK, id, userId, quantity);
    }
}
