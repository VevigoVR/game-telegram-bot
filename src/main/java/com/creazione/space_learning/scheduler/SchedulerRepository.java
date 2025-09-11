package com.creazione.space_learning.scheduler;

import com.creazione.space_learning.entities.SchedulerEntity;
import com.creazione.space_learning.enums.SchedulerType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SchedulerRepository extends JpaRepository<SchedulerEntity, Long> {
    Optional<SchedulerEntity> findByType(SchedulerType type);
}
