package com.creazione.space_learning.game.resources;

import com.creazione.space_learning.entities.game_entity.ResourceDto;
import com.creazione.space_learning.enums.Emoji;
import com.creazione.space_learning.enums.ResourceType;

public class ReferralBox1 extends ResourceDto {
    public ReferralBox1() {
        super(ResourceType.REFERRAL_BOX_1, Emoji.SCHOOL_SATCHEL);
    }

    public ReferralBox1(long quantity) {
        super(ResourceType.REFERRAL_BOX_1, Emoji.SCHOOL_SATCHEL, quantity);
    }

    public ReferralBox1(long id, long userId, long quantity) {
        super(ResourceType.REFERRAL_BOX_1, Emoji.SCHOOL_SATCHEL, id, userId, quantity);
    }
}