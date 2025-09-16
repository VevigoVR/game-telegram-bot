package com.creazione.space_learning.repository;

import com.creazione.space_learning.entities.postgres.BuildingP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface BuildingRepository extends JpaRepository<BuildingP, Long> {
    Set<BuildingP> findAllByUserId(Long id);
}
