package com.creazione.space_learning.repository;

import com.creazione.space_learning.entities.AITrainingDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;

@Repository
public interface AITrainingDataRepository extends JpaRepository<AITrainingDataEntity, Long> {
    Long countByUserIdAndTimestampAfter(Long userId, OffsetDateTime time);
}
