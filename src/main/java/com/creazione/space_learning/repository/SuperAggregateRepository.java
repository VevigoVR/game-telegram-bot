package com.creazione.space_learning.repository;

import com.creazione.space_learning.entities.SuperAggregateEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SuperAggregateRepository extends JpaRepository<SuperAggregateEntity, Long> {
    @Query("SELECT s FROM SuperAggregateEntity s")
    Page<SuperAggregateEntity> findAllByPage(Pageable pageable);
    List<SuperAggregateEntity> findAllByUserId(Long id);
}
