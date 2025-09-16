package com.creazione.space_learning.game.resources;

import com.creazione.space_learning.entities.postgres.ResourceP;
import com.creazione.space_learning.enums.ResourceType;

import java.util.List;

public class ResourceList {

    public static final List<ResourceP> RESOURCES_LIST = List.of(
            new Crypto(),
            new Coin(),
            new Knowledge(),
            new Gold(),
            new Metal(),
            new Stone(),
            new Wood(),
            new ReferralBox1(),
            new ReferralBox2(),
            new ReferralBox3(),
            new LootBoxCommon(),
            new LootBoxRare()
    );

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (ResourceP resource : RESOURCES_LIST) {
            stringBuilder.append(resource);
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

    public static ResourceP createResource(String name, Long quantity) {
        for (ResourceType type : ResourceType.values()) {
            if (type.name().equals(name)) {
                switch (type) {
                    case COIN: return new Coin(quantity);
                    case CRYPTO: return new Crypto(quantity);
                    case KNOWLEDGE: return new Knowledge(quantity);
                    case GOLD: return new Gold(quantity);
                    case METAL: return new Metal(quantity);
                    case STONE: return new Stone(quantity);
                    case WOOD: return new Wood(quantity);
                    case REFERRAL_BOX_1: return new ReferralBox1(quantity);
                    case REFERRAL_BOX_2: return new ReferralBox2(quantity);
                    case REFERRAL_BOX_3: return new ReferralBox3(quantity);
                    case LOOT_BOX_COMMON: return new LootBoxCommon(quantity);
                    case LOOT_BOX_RARE: return new LootBoxRare(quantity);

                    default: return new Unknown(quantity);
                }
            }
        }
        return new Unknown(quantity);
    }
}