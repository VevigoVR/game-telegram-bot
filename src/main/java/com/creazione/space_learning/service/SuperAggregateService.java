package com.creazione.space_learning.service;

import com.creazione.space_learning.entities.postgres.SuperAggregateP;
import com.creazione.space_learning.repository.SuperAggregateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SuperAggregateService {
    private final SuperAggregateRepository superAggregateRepository;

    public void saveAll(List<SuperAggregateP> superAggregates) {
        superAggregateRepository.saveAll(superAggregates);
    }

    public void save(SuperAggregateP superAggregate) {
        superAggregateRepository.save(superAggregate);
    }

    public Page<SuperAggregateP> findAllByPage(Pageable pageable) {
        return superAggregateRepository.findAllByPage(pageable);
    }

    public void deleteAllById(List<Long> ids) {
        superAggregateRepository.deleteAllById(ids);
    }

    public void deleteAll(List<SuperAggregateP> aggregate) {
        superAggregateRepository.deleteAll(aggregate);
    }

    public List<SuperAggregateP> findAllByUserId(Long id) {
        return superAggregateRepository.findAllByUserId(id);
    }
}