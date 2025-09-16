package com.creazione.space_learning.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@AllArgsConstructor
public enum ResourceType {
    COIN("Монета", Emoji.NAZAR_AMULET, ""),
    CRYPTO("Usdt", Emoji.MONEY, ""),
    KNOWLEDGE("Знание", Emoji.SPARKLE, ""),
    GOLD("Золото", Emoji.FULL_MOON, ""),
    METAL("Металл", Emoji.BLACK_CIRCLE, ""),
    STONE("Камень", Emoji.ROCK, ""),
    WOOD("Дерево", Emoji.WOOD, ""),
    UNKNOWN("Unknown", Emoji.ARROW_RIGHT, ""),
    REFERRAL_BOX_1("Реферальная сумка", Emoji.SCHOOL_SATCHEL, "ref 1"),
    REFERRAL_BOX_2("Реферальный сундук", Emoji.REFERRAL_BOX_2, "ref 2"),
    REFERRAL_BOX_3("Реферальный ларец", Emoji.REFERRAL_BOX_3, "ref 3"),
    LOOT_BOX_COMMON("Обычная коробка", Emoji.PACKAGE, "common"),
    LOOT_BOX_RARE("Редкая коробка", Emoji.CARD_FILE_BOX, "rare"),

    ACCELERATION_ALL(
            "Увеличение добычи всех ресурсов",
            Emoji.DIZZY, "/activate acceleration all"),
    ACCELERATION_GOLD("Увеличение добычи золота",
            Emoji.DIZZY, "/activate acceleration gold"),
    ACCELERATION_METAL(
            "Увеличение добычи металла",
            Emoji.DIZZY, "/activate acceleration metal"),
    ACCELERATION_STONE(
            "Увеличение добычи камня",
            Emoji.DIZZY, "/activate acceleration stone"),
    ACCELERATION_WOOD(
            "Увеличение добычи дерева",
            Emoji.DIZZY, "/activate acceleration wood");

    @Getter
    private final String name;
    @Getter
    private final Emoji emoji;
    @Getter
    private final String mark;

    public static List<ResourceType> getGoldBoosters() {
        return List.of(ACCELERATION_GOLD);
    }
    public static List<ResourceType> getStoneBoosters() {
        return List.of(ACCELERATION_STONE);
    }
    public static List<ResourceType> getWoodBoosters() {
        return List.of(ACCELERATION_WOOD);
    }
    public static List<ResourceType> getMetalBoosters() {
        return List.of(ACCELERATION_METAL);
    }
    public static List<ResourceType> getGeneralBoosters() { return List.of(ACCELERATION_ALL); }

    @Override
    public String toString() {
        return name;
    }

    private static final List<ResourceType> ALL_TYPES =
            Collections.unmodifiableList(Arrays.asList(values()));

    public static List<ResourceType> getAllTypes() {
        return ALL_TYPES;
    }

    public static List<ResourceType> getLootBoxTypes() {
        return List.of(REFERRAL_BOX_1, REFERRAL_BOX_2, REFERRAL_BOX_3, LOOT_BOX_COMMON, LOOT_BOX_RARE);
    }

    public static List<ResourceType> getCommonResourceTypes() {
        return List.of(COIN,
                CRYPTO,
                KNOWLEDGE,
                GOLD,
                METAL,
                STONE,
                WOOD);
    }
}