package com.creazione.space_learning.entities.game_entity;

import com.creazione.space_learning.enums.Emoji;
import com.creazione.space_learning.enums.ResourceType;
import com.creazione.space_learning.game.Item;
import com.creazione.space_learning.utils.Formatting;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ResourceDto extends Item {
    private Long id;
    private Long userId;
    private final ResourceType name;
    private final Emoji emoji;
    private long quantity;

    public ResourceDto(ResourceType name, Emoji emoji) {
        super();
        this.name = name;
        this.emoji = emoji;
        this.quantity = 0;
    }

    public ResourceDto() {
        super();
        this.name = ResourceType.UNKNOWN;
        this.emoji = Emoji.UNKNOWN;
        this.quantity = 0;
    }

    public ResourceDto(ResourceType name, Emoji emoji, long quantity) {
        super();
        this.quantity = quantity;
        this.name = name;
        this.emoji = emoji;
    }

    public ResourceDto(ResourceType name, Emoji emoji, long id, long userId, long quantity) {
        super();
        this.id = id;
        this.userId = userId;
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