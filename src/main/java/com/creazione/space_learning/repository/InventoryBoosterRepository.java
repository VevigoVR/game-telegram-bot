package com.creazione.space_learning.repository;

import com.creazione.space_learning.entities.postgres.InventoryBoosterP;
import com.creazione.space_learning.enums.ResourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface InventoryBoosterRepository extends JpaRepository<InventoryBoosterP, Long> {
    Set<InventoryBoosterP> findAllByUserId(Long id);
    Set<InventoryBoosterP> findAllByUserIdAndNameAndValueAndDurationMilli(Long id, ResourceType type, Double value, Long durationMilli);
}
