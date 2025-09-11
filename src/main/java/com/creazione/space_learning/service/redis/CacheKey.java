package com.creazione.space_learning.service.redis;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum CacheKey {
    ACTIVE_BOOSTERS_KEY("active_boosters:"),
    EMPTY_ACTIVE_BOOSTERS_KEY("empty_active_boosters:"),
    USER_KEY_PREFIX("user:"),
    BUILDING_KEY_PREFIX("user_bld:"),
    EMPTY_BUILDINGS_KEY("empty_bld:"),
    INVENTORY_BOOSTERS_KEY("inventory_boosters:"),
    EMPTY_INVENTORY_BOOSTERS_KEY("empty_inv_boosters:"),
    RESOURCE_KEY_PREFIX("user_res:"),
    EMPTY_RESOURCES_KEY("empty_res:"),
    AGGREGATE_KEY("aggregate:"),
    SEND_NOTES_KEY("send_notes:"),
    ID_TELEGRAM_MAPPING("id_telegram_mapping:");

    @Getter
    private final String name;

    @Override
    public String toString() {
        return name;
    }

    public String getKey(Long id) {
        return name + id;
    }
}
