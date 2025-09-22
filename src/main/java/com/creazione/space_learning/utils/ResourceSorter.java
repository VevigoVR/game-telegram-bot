package com.creazione.space_learning.utils;

import com.creazione.space_learning.entities.game_entity.ResourceDto;

import java.util.*;

public class ResourceSorter {
    private static final Comparator<ResourceDto> BY_NAME =
            Comparator.comparing(b -> b.getName().name(), String.CASE_INSENSITIVE_ORDER);

    public enum SortOrder {
        NAME_ASC,       // По алфавиту (A-Z)
    }

    // Основной метод сортировки
    public static List<ResourceDto> sortResources(List<ResourceDto> resources, SortOrder order) {
        if (resources == null || resources.isEmpty()) {
            return resources; // Возвращаем пустой список
        }

        List<ResourceDto> mutableList = new ArrayList<>(resources);

        switch (order) {
            case NAME_ASC:
                mutableList.sort(BY_NAME);
                break;
            default:
                mutableList.sort(BY_NAME);
        }

        return Collections.unmodifiableList(mutableList);
    }
}