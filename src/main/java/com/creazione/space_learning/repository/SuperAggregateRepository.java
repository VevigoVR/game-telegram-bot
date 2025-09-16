package com.creazione.space_learning.repository;

import com.creazione.space_learning.entities.postgres.SuperAggregateP;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SuperAggregateRepository extends JpaRepository<SuperAggregateP, Long> {
    @Query("SELECT s FROM SuperAggregateP s")
    Page<SuperAggregateP> findAllByPage(Pageable pageable);
    List<SuperAggregateP> findAllByUserId(Long id);
}
