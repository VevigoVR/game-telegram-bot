package com.creazione.space_learning.utils;

import com.creazione.space_learning.entities.postgres.ResourceP;

import java.util.*;
import java.util.stream.Collectors;

public class ResourceSorter {
    private static final Comparator<ResourceP> BY_NAME =
            Comparator.comparing(b -> b.getName().name(), String.CASE_INSENSITIVE_ORDER);

    public static Set<ResourceP> sortResourcesAsSet(Set<ResourceP> resources) {
        if (resources == null || resources.isEmpty()) {
            return resources; // Возвращаем пустой список
        }
        return resources.stream()
                .sorted(Comparator.comparing(
                        ResourceP::getName
                ))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public enum SortOrder {
        NAME_ASC,       // По алфавиту (A-Z)
    }

    // Основной метод сортировки
    public static List<ResourceP> sortResources(List<ResourceP> resources, SortOrder order) {
        if (resources == null || resources.isEmpty()) {
            return resources; // Возвращаем пустой список
        }

        List<ResourceP> mutableList = new ArrayList<>(resources);

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