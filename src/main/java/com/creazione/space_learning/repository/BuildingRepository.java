package com.creazione.space_learning.repository;

import com.creazione.space_learning.entities.Building;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface BuildingRepository extends JpaRepository<Building, Long> {
    Set<Building> findAllByUserId(Long id);
}
