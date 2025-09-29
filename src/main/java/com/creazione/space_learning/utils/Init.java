package com.creazione.space_learning.utils;

import com.creazione.space_learning.config.DataSet;
import com.creazione.space_learning.entities.game_entity.UserDto;
import com.creazione.space_learning.entities.postgres.AggregateNoticeP;
import com.creazione.space_learning.entities.postgres.InventoryBoosterP;
import com.creazione.space_learning.enums.NoticeType;
import com.creazione.space_learning.enums.ResourceType;
import com.creazione.space_learning.service.AggregateNoticeService;
import com.creazione.space_learning.service.postgres.UserPostgresService;

import java.time.Duration;
import java.util.*;

public class Init {
    private AggregateNoticeService aggregateNoticeService;
    private UserPostgresService userService;

    public Init() {
        this.aggregateNoticeService = DataSet.getAggregateNoticeService();
        this.userService = DataSet.getUserService();
    }

    public void init() {
        //createNotices();
        //createReferral();
        addGifts();
    }

    private void addGifts() {
        Set<InventoryBoosterP> inventoryBoosters = Set.of(new InventoryBoosterP(
                        ResourceType.ACCELERATION_METAL,
                        0.2, Duration.ofHours(1).toMillis(), 10),
                new InventoryBoosterP(
                        ResourceType.ACCELERATION_STONE,
                        0.2, Duration.ofHours(1).toMillis(), 10),
                new InventoryBoosterP(
                        ResourceType.ACCELERATION_ALL,
                        0.2, Duration.ofHours(1).toMillis(), 10));
        inventoryBoosters.forEach(booster -> booster.setUserId(1L));
        DataSet.getBoosterService().saveAllIB(inventoryBoosters, 5773183764L);
    }

    private void createNotices() {
        //aggregateNoticeService.deleteAll();
        List<AggregateNoticeP> aggregateNoticeEntityList = new ArrayList<>();
        AggregateNoticeP aggregateNotice1 = new AggregateNoticeP(1L, NoticeType.GIFT_TO_REFERRAL,  5L);
        aggregateNotice1.setCreatedAt(getLaterDate());
        Map<String, Long> resources = Map.of(ResourceType.REFERRAL_BOX_1.name(), 5L);
        aggregateNotice1.setResources(resources);
        aggregateNoticeEntityList.add(aggregateNotice1);

        AggregateNoticeP aggregateNotice2 = new AggregateNoticeP(1L, NoticeType.GIFT_TO_REFERRAL,  1L);
        Map<String, Long> resources2 = new HashMap<>();
        resources2.put(ResourceType.REFERRAL_BOX_1.name(), 5L);
        resources2.put(ResourceType.CRYPTO.name(), 58L);
        aggregateNotice2.setCreatedAt(getLaterDate());
        aggregateNotice2.setResources(resources2);
        aggregateNoticeEntityList.add(aggregateNotice2);

        AggregateNoticeP aggregateNotice3 = new AggregateNoticeP(1L, NoticeType.GIFT_TO_REFERRER,  5L);
        Map<String, Long> resources3 = Map.of(ResourceType.REFERRAL_BOX_1.name(), 5L);
        aggregateNotice3.setCreatedAt(getLaterDate());
        aggregateNotice3.setResources(resources3);
        aggregateNoticeEntityList.add(aggregateNotice3);

        AggregateNoticeP aggregateNotice4 = new AggregateNoticeP(1L, NoticeType.GIFT_TO_REFERRER,  5L);
        Map<String, Long> resources4 = Map.of(ResourceType.COIN.name(), 5L);
        aggregateNotice4.setCreatedAt(new Date());
        aggregateNotice4.setResources(resources4);
        aggregateNoticeEntityList.add(aggregateNotice4);

        AggregateNoticeP aggregateNotice5 = new AggregateNoticeP(1L, NoticeType.GIFT_TO_REFERRAL,  7L);
        aggregateNotice5.setCreatedAt(getLaterDate());
        Map<String, Long> resources5 = Map.of(ResourceType.GOLD.name(), 5L);
        aggregateNotice5.setResources(resources5);
        aggregateNoticeEntityList.add(aggregateNotice5);

        aggregateNoticeService.saveAll(aggregateNoticeEntityList);

    }

    private Date getLaterDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -8);
        return calendar.getTime();
    }

    public void createReferral() {
        UserDto userEntity = userService.findById(1L);
        if (userEntity != null) {
            userEntity.setTotalReferrals(888);
            userService.saveFullWithoutCache(userEntity);
        }
    }
}
