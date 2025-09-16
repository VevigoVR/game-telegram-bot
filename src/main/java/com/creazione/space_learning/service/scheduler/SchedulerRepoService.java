package com.creazione.space_learning.service.scheduler;

import com.creazione.space_learning.entities.postgres.SchedulerP;
import com.creazione.space_learning.enums.SchedulerType;
import com.creazione.space_learning.service.redis.SchedulerCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SchedulerRepoService {
    private final SchedulerRepository schedulerRepository;
    private final SchedulerCacheService schedulerCacheService;

    public SchedulerP findByType(SchedulerType type) {
        if (type.equals(SchedulerType.SEND_SUPER_MESSAGES)) {
            SchedulerP scheduler = schedulerCacheService.getSendScheduler();
            if (scheduler != null) {
                return scheduler;
            }
            Optional<SchedulerP> schedulerEntity = schedulerRepository.findByType(type);
            if (schedulerEntity.isPresent()) {
                schedulerCacheService.cacheSendScheduler(schedulerEntity.get());
                return schedulerEntity.get();
            }
        } else if (type.equals(SchedulerType.SUPER_AGGREGATE)) {
            SchedulerP scheduler = schedulerCacheService.getAggregateScheduler();
            if (scheduler != null) {
                return scheduler;
            }
            Optional<SchedulerP> schedulerEntity = schedulerRepository.findByType(type);
            if (schedulerEntity.isPresent()) {
                schedulerCacheService.cacheAggregateScheduler(schedulerEntity.get());
                return schedulerEntity.get();
            }
        }
        return null;
    }

    public void save(SchedulerP scheduler) {
        if (scheduler.getType().equals(SchedulerType.SEND_SUPER_MESSAGES)) {
            schedulerCacheService.deleteSendScheduler();
            schedulerCacheService.cacheSendScheduler(schedulerRepository.save(scheduler));
        } else if (scheduler.getType().equals(SchedulerType.SUPER_AGGREGATE)) {
            schedulerCacheService.deleteAggregateScheduler();
            schedulerCacheService.cacheAggregateScheduler(schedulerRepository.save(scheduler));
        }
    }
}
