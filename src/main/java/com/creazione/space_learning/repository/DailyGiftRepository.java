package com.creazione.space_learning.repository;

import com.creazione.space_learning.entities.postgres.DailyGiftP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DailyGiftRepository extends JpaRepository<DailyGiftP, Long> {
    List<DailyGiftP> findAllByUserId(Long id);
    DailyGiftP findByUserId(Long id);
}
