package com.creazione.space_learning.game.resources;

import com.creazione.space_learning.entities.game_entity.ResourceDto;
import com.creazione.space_learning.enums.Emoji;
import com.creazione.space_learning.enums.ResourceType;

public class ReferralBox2 extends ResourceDto {
    public ReferralBox2() {
        super(ResourceType.REFERRAL_BOX_2, Emoji.REFERRAL_BOX_2);
    }

    public ReferralBox2(long quantity) {
        super(ResourceType.REFERRAL_BOX_2, Emoji.REFERRAL_BOX_2, quantity);
    }

    public ReferralBox2(long id, long userId, long quantity) {
        super(ResourceType.REFERRAL_BOX_2, Emoji.REFERRAL_BOX_2, id, userId, quantity);
    }
}