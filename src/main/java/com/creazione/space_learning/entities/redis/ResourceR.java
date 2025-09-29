package com.creazione.space_learning.entities.redis;

import com.creazione.space_learning.enums.Emoji;
import com.creazione.space_learning.enums.ResourceType;
import com.creazione.space_learning.game.Item;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResourceR extends Item {
    private Long id;
    private Long userId;
    @JsonProperty("name")
    private ResourceType name;
    @JsonProperty("emoji")
    private Emoji emoji;
    private long quantity;
}