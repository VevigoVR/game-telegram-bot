package com.creazione.space_learning.dto;

import com.creazione.space_learning.enums.ResourceType;
import com.creazione.space_learning.game.Item;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OpenLootBoxDto {
    private String[] args;
    private String wrong = "";
    private List<Item> items;
    private ResourceType resourceType;
}
