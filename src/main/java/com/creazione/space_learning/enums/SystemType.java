package com.creazione.space_learning.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SystemType {
    PLUG("ЗАГЛУШКА");

    private final String name;

    @Override
    public String toString() {
        return name;
    }

    public static String wrong(SystemType type, Object o) {
        return type.getName() + " " + o;
    }
}
