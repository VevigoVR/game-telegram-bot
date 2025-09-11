package com.creazione.space_learning.utils;

import com.creazione.space_learning.entities.InventoryBooster;

import java.util.*;
import java.util.stream.Collectors;

public class InventoryBoosterSorter {
    private static final Comparator<InventoryBooster> BY_NAME =
            Comparator.comparing(b -> b.getName().name(), String.CASE_INSENSITIVE_ORDER);

    public static Set<InventoryBooster> sortResourcesAsSet(Set<InventoryBooster> resources) {
        if (resources == null || resources.isEmpty()) {
            return resources; // Возвращаем пустой список
        }
        return resources.stream()
                .sorted(Comparator.comparing(
                        InventoryBooster::getName
                ))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public enum SortOrder {
        NAME_ASC,       // По алфавиту (A-Z)
    }

    // Основной метод сортировки
    public static List<InventoryBooster> sortResources(List<InventoryBooster> resources, SortOrder order) {
        if (resources == null || resources.isEmpty()) {
            return resources; // Возвращаем пустой список
        }

        List<InventoryBooster> mutableList = new ArrayList<>(resources);

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
