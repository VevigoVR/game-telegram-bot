package com.creazione.space_learning.game.resources;

import com.creazione.space_learning.entities.game_entity.ResourceDto;
import com.creazione.space_learning.enums.Emoji;
import com.creazione.space_learning.enums.ResourceType;
import lombok.Getter;

@Getter
public class Metal extends ResourceDto {

    private final double buyForGold = 0.8;
    private final double sellForGold = 0.75;
    private final boolean forTrade = true;

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