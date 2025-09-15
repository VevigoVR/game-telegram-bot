package com.creazione.space_learning.service.scheduler;

import com.creazione.space_learning.config.DataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class Scheduler {
    private static final Logger log = LoggerFactory.getLogger(Scheduler.class);
    private final SchedulerService scoreService;

    public Scheduler(SchedulerService scoreService) {
        this.scoreService = scoreService;
    }

    //@Scheduled(fixedRate = 60_000) // запуск раз в 1 минуту (60_000)
    @Scheduled(cron = "0 0 3 * * ?")
    public void scoresGrantsAggregates() throws InterruptedException {
        Instant start = Instant.now();
        log.info("Starting score calculation job");
        DataSet.setMaintenance(true);
        //Thread.sleep(60_000);
        try {
            scoreService.scoresGrantsSuperAggregate();
            scoreService.aggregateNotices();
            log.info("Score calculation completed successfully");
        } catch (Exception e) {
            log.error("Score calculation failed", e);
        }
        Duration duration = Duration.between(start, Instant.now());
        log.info("⏱ Время выполнения: {} сек", duration.getSeconds());
        //Thread.sleep(60_000);
        DataSet.setMaintenance(false);
    }

    //@Scheduled(fixedRate = 30_000)
    @Scheduled(cron = "0 0 9 * * ?")
    public void sendSuperNotices() {
        Instant start = Instant.now();
        log.info("Starting sending super notices");
        try {
            scoreService.sendSuperNotices();
            log.info("send super notices completed successfully");
        } catch (Exception e) {
            log.error("send super notices failed", e);
        }
        Duration duration = Duration.between(start, Instant.now());
        log.info("⏱ Время выполнения: {} сек", duration.getSeconds());
    }
}