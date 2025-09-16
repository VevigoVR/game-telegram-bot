package com.creazione.space_learning.service.scheduler;

import com.creazione.space_learning.entities.postgres.SchedulerP;
import com.creazione.space_learning.enums.SchedulerType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SchedulerRepository extends JpaRepository<SchedulerP, Long> {
    Optional<SchedulerP> findByType(SchedulerType type);
}
