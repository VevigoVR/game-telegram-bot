package com.creazione.space_learning.utils;

import com.creazione.space_learning.entities.Building;

import java.util.*;
import java.util.stream.Collectors;

public class BuildingSorter {
    private static final Comparator<Building> BY_NAME =
            Comparator.comparing(b -> b.getName().name(), String.CASE_INSENSITIVE_ORDER);

    private static final Comparator<Building> BY_LEVEL_DESC =
            Comparator.comparingInt(Building::getLevel).reversed();

    public static Set<Building> sortBuildingsAsSet(Set<Building> buildings) {
        if (buildings == null || buildings.isEmpty()) {
            return buildings;
        }
        return buildings.stream()
                .sorted(Comparator.comparing(
                        Building::getName
                ))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    // Основной метод сортировки
    public static List<Building> sortBuildings(List<Building> buildings, SortOrder order) {
        if (buildings == null || buildings.isEmpty()) {
            return buildings; // Возвращаем пустой неизменяемый список
        }

        List<Building> mutableList = new ArrayList<>(buildings);

        switch (order) {
            case NAME_ASC:
                mutableList.sort(BY_NAME);
                break;
            case LEVEL_DESC:
                mutableList.sort(BY_LEVEL_DESC);
                break;
            case NAME_THEN_LEVEL:
                mutableList.sort(BY_NAME.thenComparing(BY_LEVEL_DESC));
                break;
            default:
                mutableList.sort(BY_NAME);
        }

        return Collections.unmodifiableList(mutableList);
    }

    public enum SortOrder {
        NAME_ASC,       // По алфавиту (A-Z)
        LEVEL_DESC,      // По уровню (высокий→низкий)
        NAME_THEN_LEVEL  // По имени, затем по уровню
    }
}