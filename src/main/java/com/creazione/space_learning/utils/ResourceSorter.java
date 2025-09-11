package com.creazione.space_learning.utils;

import com.creazione.space_learning.entities.Resource;

import java.util.*;
import java.util.stream.Collectors;

public class ResourceSorter {
    private static final Comparator<Resource> BY_NAME =
            Comparator.comparing(b -> b.getName().name(), String.CASE_INSENSITIVE_ORDER);

    public static Set<Resource> sortResourcesAsSet(Set<Resource> resources) {
        if (resources == null || resources.isEmpty()) {
            return resources; // Возвращаем пустой список
        }
        return resources.stream()
                .sorted(Comparator.comparing(
                        Resource::getName
                ))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public enum SortOrder {
        NAME_ASC,       // По алфавиту (A-Z)
    }

    // Основной метод сортировки
    public static List<Resource> sortResources(List<Resource> resources, SortOrder order) {
        if (resources == null || resources.isEmpty()) {
            return resources; // Возвращаем пустой список
        }

        List<Resource> mutableList = new ArrayList<>(resources);

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