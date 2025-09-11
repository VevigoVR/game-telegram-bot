package com.creazione.space_learning.repository;

import com.creazione.space_learning.entities.InventoryBooster;
import com.creazione.space_learning.enums.ResourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface InventoryBoosterRepository extends JpaRepository<InventoryBooster, Long> {
    Set<InventoryBooster> findAllByUserId(Long id);
    Set<InventoryBooster> findAllByUserIdAndNameAndValueAndDurationMilli(Long id, ResourceType type, Double value, Long durationMilli);
}
