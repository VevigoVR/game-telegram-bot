package com.creazione.space_learning.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum BuildingType {
    METAL_BUILDING("Рудник металла"),
    GOLD_BUILDING("Золотая шахта"),
    STONE_BUILDING("Каменный карьер"),
    WOOD_BUILDING("Лесопилка"),
    STORAGE_BUILDING("Склад"),
    UNKNOWN("unknown");
    ;
    private final String name;

    @Override
    public String toString() {
        return name;
    }
}
