package com.creazione.space_learning.repository;

import com.creazione.space_learning.entities.ActiveBooster;
import com.creazione.space_learning.enums.ResourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActiveBoosterRepository  extends JpaRepository<ActiveBooster, Long> {
    List<ActiveBooster> findAllByUserId(Long id);
    List<ActiveBooster> findAllByUserIdAndNameIn(Long id, List<ResourceType> types);
}
