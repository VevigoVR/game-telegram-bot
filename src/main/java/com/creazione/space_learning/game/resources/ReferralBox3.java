package com.creazione.space_learning.game.resources;

import com.creazione.space_learning.entities.Resource;
import com.creazione.space_learning.enums.Emoji;
import com.creazione.space_learning.enums.ResourceType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("REFERRAL_BOX_3")
public class ReferralBox3 extends Resource {
    public ReferralBox3() {
        super(ResourceType.REFERRAL_BOX_3, Emoji.REFERRAL_BOX_3);
    }
    public ReferralBox3(double quantity) {
        super(ResourceType.REFERRAL_BOX_3, Emoji.REFERRAL_BOX_3, quantity);
    }
}