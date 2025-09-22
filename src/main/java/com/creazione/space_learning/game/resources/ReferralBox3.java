package com.creazione.space_learning.game.resources;

import com.creazione.space_learning.entities.game_entity.ResourceDto;
import com.creazione.space_learning.enums.Emoji;
import com.creazione.space_learning.enums.ResourceType;

public class ReferralBox3 extends ResourceDto {
    public ReferralBox3() {
        super(ResourceType.REFERRAL_BOX_3, Emoji.REFERRAL_BOX_3);
    }

    public ReferralBox3(long quantity) {
        super(ResourceType.REFERRAL_BOX_3, Emoji.REFERRAL_BOX_3, quantity);
    }

    public ReferralBox3(long id, long userId, long quantity) {
        super(ResourceType.REFERRAL_BOX_3, Emoji.REFERRAL_BOX_3, id, userId, quantity);
    }
}