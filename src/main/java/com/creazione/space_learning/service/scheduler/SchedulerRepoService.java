package com.creazione.space_learning.service.scheduler;

import com.creazione.space_learning.entities.SchedulerEntity;
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

    public SchedulerEntity findByType(SchedulerType type) {
        if (type.equals(SchedulerType.SEND_SUPER_MESSAGES)) {
            SchedulerEntity scheduler = schedulerCacheService.getSendScheduler();
            if (scheduler != null) {
                return scheduler;
            }
            Optional<SchedulerEntity> schedulerEntity = schedulerRepository.findByType(type);
            if (schedulerEntity.isPresent()) {
                schedulerCacheService.cacheSendScheduler(schedulerEntity.get());
                return schedulerEntity.get();
            }
        } else if (type.equals(SchedulerType.SUPER_AGGREGATE)) {
            SchedulerEntity scheduler = schedulerCacheService.getAggregateScheduler();
            if (scheduler != null) {
                return scheduler;
            }
            Optional<SchedulerEntity> schedulerEntity = schedulerRepository.findByType(type);
            if (schedulerEntity.isPresent()) {
                schedulerCacheService.cacheAggregateScheduler(schedulerEntity.get());
                return schedulerEntity.get();
            }
        }
        return null;
    }

    public void save(SchedulerEntity scheduler) {
        if (scheduler.getType().equals(SchedulerType.SEND_SUPER_MESSAGES)) {
            schedulerCacheService.deleteSendScheduler();
            schedulerCacheService.cacheSendScheduler(schedulerRepository.save(scheduler));
        } else if (scheduler.getType().equals(SchedulerType.SUPER_AGGREGATE)) {
            schedulerCacheService.deleteAggregateScheduler();
            schedulerCacheService.cacheAggregateScheduler(schedulerRepository.save(scheduler));
        }
    }
}
