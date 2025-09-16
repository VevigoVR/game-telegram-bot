package com.creazione.space_learning.service.redis;

import com.creazione.space_learning.entities.redis.SchedulerR;
import com.creazione.space_learning.entities.postgres.SchedulerP;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static com.creazione.space_learning.service.redis.CacheKey.*;

@Service
@RequiredArgsConstructor
public class SchedulerCacheService {
    private final RedisTemplate<String, Object> redisTemplate;

    // Методы преобразования Entity <-> DTO
    private SchedulerR toRedisObject(SchedulerP scheduler) {
        return new SchedulerR(
                scheduler.getId(),
                scheduler.getType(),
                scheduler.isRun(),
                scheduler.getLastDuration(),
                scheduler.getPreviousDuration(),
                scheduler.getLastStart(),
                scheduler.getPreviousStart(),
                scheduler.getLastEnd(),
                scheduler.getPreviousEnd()
        );
    }

    private SchedulerP toGameObject(SchedulerR dto) {
        SchedulerP scheduler = new SchedulerP();
        scheduler.setId(dto.getId());
        scheduler.setType(dto.getType());
        scheduler.setRun(dto.isRun());
        scheduler.setLastDuration(dto.getLastDuration());
        scheduler.setPreviousDuration(dto.getPreviousDuration());
        scheduler.setLastStart(dto.getLastStart());
        scheduler.setPreviousStart(dto.getPreviousStart());
        scheduler.setLastEnd(dto.getLastEnd());
        scheduler.setPreviousEnd(dto.getPreviousEnd());
        return scheduler;
    }

    public void cacheAggregateScheduler(SchedulerP scheduler) {
        deleteAggregateScheduler();
        SchedulerR dto = toRedisObject(scheduler);
        redisTemplate.opsForValue().set(AGGREGATE_KEY.getName(), dto);
        redisTemplate.expire(AGGREGATE_KEY.getName(), 1, TimeUnit.HOURS);
    }

    public void cacheSendScheduler(SchedulerP scheduler) {
        deleteSendScheduler();
        SchedulerR dto = toRedisObject(scheduler);
        redisTemplate.opsForValue().set(SEND_NOTES_KEY.getName(), dto);
        redisTemplate.expire(SEND_NOTES_KEY.getName(), 1, TimeUnit.HOURS);

    }

    public SchedulerP getAggregateScheduler() {
        SchedulerR dto = (SchedulerR) redisTemplate.opsForValue().get(AGGREGATE_KEY.getName());
        return dto != null ? toGameObject(dto) : null;
    }

    public SchedulerP getSendScheduler() {
        SchedulerR dto = (SchedulerR) redisTemplate.opsForValue().get(SEND_NOTES_KEY.getName());
        return dto != null ? toGameObject(dto) : null;
    }

    // Остальные методы остаются без изменений
    public void deleteAggregateScheduler() {
        redisTemplate.delete(AGGREGATE_KEY.getName());
    }

    public void deleteSendScheduler() {
        redisTemplate.delete(SEND_NOTES_KEY.getName());
    }
}