package com.creazione.space_learning.game.resources;

import com.creazione.space_learning.entities.postgres.ResourceP;
import com.creazione.space_learning.enums.Emoji;
import com.creazione.space_learning.enums.ResourceType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("REFERRAL_BOX_2")
public class ReferralBox2 extends ResourceP {
    public ReferralBox2() {
        super(ResourceType.REFERRAL_BOX_2, Emoji.REFERRAL_BOX_2);
    }
    public ReferralBox2(double quantity) {
        super(ResourceType.REFERRAL_BOX_2, Emoji.REFERRAL_BOX_2, quantity);
    }
}