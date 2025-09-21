package com.creazione.space_learning.entities.postgres;

import com.creazione.space_learning.enums.ResourceType;
import com.creazione.space_learning.game.Item;
import com.creazione.space_learning.game.resources.*;
import com.creazione.space_learning.enums.Emoji;
import com.creazione.space_learning.utils.Formatting;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "resources")
@AllArgsConstructor
// Resource.java
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "resourceType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Gold.class, name = "GOLD"),
        @JsonSubTypes.Type(value = Metal.class, name = "METAL"),
        @JsonSubTypes.Type(value = Crypto.class, name = "CRYPTO"),
        @JsonSubTypes.Type(value = Knowledge.class, name = "KNOWLEDGE"),
        @JsonSubTypes.Type(value = LootBoxCommon.class, name = "LOOT_BOX_COMMON"),
        @JsonSubTypes.Type(value = LootBoxRare.class, name = "LOOT_BOX_RARE"),
        @JsonSubTypes.Type(value = Coin.class, name = "COIN"),
        @JsonSubTypes.Type(value = Stone.class, name = "STONE"),
        @JsonSubTypes.Type(value = Wood.class, name = "WOOD"),
        @JsonSubTypes.Type(value = ReferralBox1.class, name = "REFERRAL_BOX_1"),
        @JsonSubTypes.Type(value = ReferralBox2.class, name = "REFERRAL_BOX_2"),
        @JsonSubTypes.Type(value = ReferralBox3.class, name = "REFERRAL_BOX_3"),
        @JsonSubTypes.Type(value = Unknown.class, name = "UNKNOWN")
})
@DiscriminatorColumn(name = "resource_type", discriminatorType = DiscriminatorType.STRING)
public class ResourceP extends Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Enumerated(EnumType.STRING)
    private final ResourceType name;
    @Enumerated(EnumType.STRING)
    private final Emoji emoji;
    private long quantity;

    public ResourceP(ResourceType name, Emoji emoji) {
        super();
        this.name = name;
        this.emoji = emoji;
        this.quantity = 0;
    }

    public ResourceP() {
        super();
        this.name = ResourceType.UNKNOWN;
        this.emoji = Emoji.UNKNOWN;
        this.quantity = 0;
    }

    public ResourceP(ResourceType name, Emoji emoji, long quantity) {
        super();
        this.quantity = quantity;
        this.name = name;
        this.emoji = emoji;
    }

    public String makeQuantityString() {
        //DecimalFormat df = new DecimalFormat("0.#");
        //return df.format(quantity);
        return Formatting.formatWithDots(this.quantity);
    }

    public void incrementQuantity() {
        this.quantity++;
    }

    @Override
    public void addQuantity(long quantity) {
        this.quantity += quantity;
    }

    public void subtractQuantity(long quantity) {
        this.quantity -= quantity;
    }

    @Override
    public String toString() {
        return  this.getName() + ": " + this.makeQuantityString();
    }
}