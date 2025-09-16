package com.creazione.space_learning.game.resources;

import com.creazione.space_learning.entities.postgres.ResourceP;
import com.creazione.space_learning.enums.Emoji;
import com.creazione.space_learning.enums.ResourceType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("CRYPTO")
public class Crypto extends ResourceP {
    public Crypto() {
        super(ResourceType.CRYPTO, Emoji.MONEY);
    }
    public Crypto(double quantity) {
        super(ResourceType.CRYPTO, Emoji.MONEY, quantity);
    }
}