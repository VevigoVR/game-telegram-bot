package com.creazione.space_learning.repository;

import com.creazione.space_learning.entities.postgres.ActiveBoosterP;
import com.creazione.space_learning.enums.ResourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActiveBoosterRepository  extends JpaRepository<ActiveBoosterP, Long> {
    List<ActiveBoosterP> findAllByUserId(Long id);
    List<ActiveBoosterP> findAllByUserIdAndNameIn(Long id, List<ResourceType> types);
}
