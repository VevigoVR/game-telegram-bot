package com.creazione.space_learning.repository;

import com.creazione.space_learning.entities.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface ResourcesRepository extends JpaRepository<Resource, Long> {
    Set<Resource> findAllByUserId(Long id);
}
