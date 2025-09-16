package com.creazione.space_learning.repository;

import com.creazione.space_learning.entities.postgres.ResourceP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface ResourcesRepository extends JpaRepository<ResourceP, Long> {
    Set<ResourceP> findAllByUserId(Long id);
}
