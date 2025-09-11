package com.creazione.space_learning.service;

import com.creazione.space_learning.entities.SuperAggregateEntity;
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

    public void saveAll(List<SuperAggregateEntity> superAggregates) {
        superAggregateRepository.saveAll(superAggregates);
    }

    public void save(SuperAggregateEntity superAggregate) {
        superAggregateRepository.save(superAggregate);
    }

    public Page<SuperAggregateEntity> findAllByPage(Pageable pageable) {
        return superAggregateRepository.findAllByPage(pageable);
    }

    public void deleteAllById(List<Long> ids) {
        superAggregateRepository.deleteAllById(ids);
    }

    public void deleteAll(List<SuperAggregateEntity> aggregate) {
        superAggregateRepository.deleteAll(aggregate);
    }

    public List<SuperAggregateEntity> findAllByUserId(Long id) {
        return superAggregateRepository.findAllByUserId(id);
    }
}