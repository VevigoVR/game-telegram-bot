package com.creazione.space_learning.repository;

import com.creazione.space_learning.entities.DailyGiftEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DailyGiftRepository extends JpaRepository<DailyGiftEntity, Long> {
    List<DailyGiftEntity> findAllByUserId(Long id);
    DailyGiftEntity findByUserId(Long id);
}
