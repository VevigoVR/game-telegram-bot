package com.creazione.space_learning.config;

import com.creazione.space_learning.service.BoosterService;
import com.creazione.space_learning.scheduler.SchedulerRepoService;
import com.creazione.space_learning.service.*;
import com.creazione.space_learning.utils.ThrottledMessageSender4;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class DataSet {
    @Getter
    private static UserService userService;
    @Getter
    private static BuildingService buildingService;
    @Getter
    private static ResourceService resourceService;
    @Getter
    private static ReferralService referralService;
    @Getter
    private static NoticeService noticeService;
    @Getter
    private static AggregateNoticeService aggregateNoticeService;
    @Getter
    private static SuperAggregateService superAggregateService;
    @Getter
    private static SchedulerRepoService schedulerRepoService;
    @Getter
    private static BoosterService boosterService;
    @Getter
    private static LootBoxService lootBoxService;
    @Getter
    private static DailyGiftService dailyGiftService;

    @Setter
    @Getter
    volatile private static boolean maintenance = false;

    @Setter
    @Getter
    private static ThrottledMessageSender4 throttledSender4;

    public DataSet (
            UserService userService,
            BuildingService buildingService,
            ResourceService resourceService,
            ReferralService referralService,
            NoticeService noticeService,
            AggregateNoticeService aggregateNoticeService,
            SuperAggregateService superAggregateService,
            SchedulerRepoService schedulerRepoService,
            BoosterService boosterService,
            LootBoxService lootBoxService,
            DailyGiftService dailyGiftService
    ) {
        // Проверка на null для каждого сервиса
        if (userService == null
                || buildingService == null
                || resourceService == null
                || referralService == null
                || noticeService == null
                || aggregateNoticeService == null
                || superAggregateService == null
                || schedulerRepoService == null
                || boosterService == null
                || lootBoxService == null
                || dailyGiftService == null) {
            throw new IllegalStateException("Критическая ошибка: сервисы не инициализированы!");
        }

        DataSet.userService = userService;
        DataSet.buildingService = buildingService;
        DataSet.resourceService = resourceService;
        DataSet.referralService = referralService;
        DataSet.noticeService = noticeService;
        DataSet.aggregateNoticeService = aggregateNoticeService;
        DataSet.superAggregateService = superAggregateService;
        DataSet.schedulerRepoService = schedulerRepoService;
        DataSet.boosterService = boosterService;
        DataSet.lootBoxService = lootBoxService;
        DataSet.dailyGiftService = dailyGiftService;

        System.out.println("✅ DataSet успешно инициализирован!");
    }
}