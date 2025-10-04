package com.creazione.space_learning.dto;

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
public class RandomItemDto {
    private String wrongText = "";
    private List<Item> items;
}
