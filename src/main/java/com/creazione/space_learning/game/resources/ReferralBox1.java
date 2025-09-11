package com.creazione.space_learning.game.resources;

import com.creazione.space_learning.entities.Resource;
import com.creazione.space_learning.enums.Emoji;
import com.creazione.space_learning.enums.ResourceType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("REFERRAL_BOX_1")
public class ReferralBox1 extends Resource {
    public ReferralBox1() {
        super(ResourceType.REFERRAL_BOX_1, Emoji.SCHOOL_SATCHEL);
    }

    public ReferralBox1(double quantity) {
        super(ResourceType.REFERRAL_BOX_1, Emoji.SCHOOL_SATCHEL, quantity);
    }
}