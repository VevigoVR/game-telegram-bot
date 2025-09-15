package com.creazione.space_learning.service.scheduler;

import com.creazione.space_learning.config.DataSet;
import com.creazione.space_learning.dto.UserDto;
import com.creazione.space_learning.entities.*;
import com.creazione.space_learning.enums.NoticeType;
import com.creazione.space_learning.entities.Building;
import com.creazione.space_learning.entities.Resource;
import com.creazione.space_learning.enums.SchedulerType;
import com.creazione.space_learning.game.resources.ResourceList;
import com.creazione.space_learning.enums.ResourceType;
import com.creazione.space_learning.queries.responces.Response;
import com.creazione.space_learning.queries.responces.SuperAggregateMessage;
import com.creazione.space_learning.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulerService {
    private static final UserService userService = DataSet.getUserService();
    private static final BuildingService buildingService = DataSet.getBuildingService();
    private static final ResourceService resourceService = DataSet.getResourceService();
    private static final NoticeService noticeService = DataSet.getNoticeService();
    private static final AggregateNoticeService aggregateNoticeService = DataSet.getAggregateNoticeService();
    private static final SuperAggregateService superAggregateService = DataSet.getSuperAggregateService();
    private static final SchedulerRepoService schedulerRepoService = DataSet.getSchedulerRepoService();

    public static Date getLaterDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -7);
        return calendar.getTime();
    }

    // Формула расчета очков
    private long calculateScore(List<Building> buildings) {
        return buildingService.getPointsForAllBuildings(buildings);
    }

    @Transactional
    public void scoresGrantsSuperAggregate() {
        SchedulerType schedulerType = SchedulerType.SUPER_AGGREGATE;
        startSchedulerTracing(schedulerType);

        int page = 0;
        int batchSize = 100;
        //Date cutoff = Date.from(Instant.now().minus(100, ChronoUnit.HOURS));
        Page<UserEntity> usersPage;

        do {
            // Пагинация для обработки больших объемов данных
            usersPage = userService.findUsersWithResourcesAndRecentBuildingUpdates(
                    PageRequest.of(page, batchSize, Sort.by("id"))
            );
            System.out.println("Количество найденных пользователей: " + usersPage.getContent().size());


            List<Long> ids = chooseUserIds(usersPage.getContent());

            // Поиск всех непрочтённых аггрегированных уведомлений
            List<AggregateNoticeEntity> aggregateNotices = aggregateNoticeService.findGroupedNotices(ids, false);

            List<UserEntity> userEntities = new ArrayList<>();
            for (UserEntity userEntity : usersPage.getContent()) {
                List<Building> buildings = userEntity.getBuildings().stream().toList();
                resourceService.calculateQuantityChanges(userEntity, Instant.now());

                if (!aggregateNotices.isEmpty()) {
                    // Обработка всех аггрегированных уведомлений
                    if (userEntity.isSuperAggregate()) {
                        grantOldNotices(userEntity, aggregateNotices);
                    } else {
                        saveToSendMessageIfOldNoticesHere(userEntity, aggregateNotices);
                    }
                }

                PlayerScore playerScore = userEntity.getPlayerScore();
                long oldScore = playerScore.getScore();
                long newScore = calculateScore(buildings);
                grantReferralGifts(userEntity, oldScore, newScore);

                playerScore.setUserId(userEntity.getId());
                playerScore.setScore(newScore);
                playerScore.setCalculatedAt(Instant.now());
                userEntities.add(userEntity);
            }
            userService.saveAll(userEntities);
            page++;
            log.info("Processed {} players for calculateAllPlayerScores", page * batchSize);
        } while (usersPage.hasNext());

        endSchedulerTracing(schedulerType);
    }

    private void startSchedulerTracing(SchedulerType type) {
        SchedulerEntity scheduler = schedulerRepoService.findByType(type);
        scheduler = createSchedulerEntity(scheduler, type);
        schedulerRepoService.save(scheduler);
    }

    private void endSchedulerTracing(SchedulerType type) {
        SchedulerEntity scheduler = schedulerRepoService.findByType(type);
        closeSchedulerEntity(scheduler);
        schedulerRepoService.save(scheduler);
    }

    private SchedulerEntity createSchedulerEntity(SchedulerEntity scheduler, SchedulerType type) {
        if (scheduler == null) {
            SchedulerEntity schedulerFirst = new SchedulerEntity();
            schedulerFirst.setType(type);
            schedulerFirst.setRun(true);
            schedulerFirst.setLastDuration(0L);
            schedulerFirst.setPreviousDuration(0L);
            schedulerFirst.setLastStart(Instant.now());
            return schedulerFirst;
        }

        Long lastDuration = scheduler.getLastDuration();
        Instant lastStart = scheduler.getLastStart();
        Instant lastEnd = scheduler.getLastEnd();
        scheduler.setRun(true);
        scheduler.setLastDuration(0L);
        scheduler.setPreviousDuration(lastDuration);
        scheduler.setLastStart(Instant.now());
        scheduler.setPreviousStart(lastStart);
        scheduler.setPreviousEnd(lastEnd);
        return scheduler;
    }

    private void closeSchedulerEntity(SchedulerEntity scheduler) {
        if (scheduler == null) { return; }
        Instant dateNow = Instant.now();
        long lastDuration = dateNow.toEpochMilli() - scheduler.getLastStart().toEpochMilli();
        scheduler.setRun(false);
        scheduler.setLastDuration(lastDuration);
        scheduler.setLastEnd(dateNow);
    }

    private void grantReferralGifts(UserEntity userEntity, long oldScore, long newScore) {
        Long referrerId = userEntity.getReferrer();

        if (referrerId == null || referrerId == 0) {
            return;
        }

        UserEntity userReferrer = userService.findById(referrerId);
        if (oldScore < 5 && newScore >= 5) {
            // если есть реферрер, то и ему начисляем очки
            if (userReferrer != null) {
                noticeService.save(new NoticeEntity(userReferrer.getId(), NoticeType.GIFT_TO_REFERRER, ResourceType.REFERRAL_BOX_1));
            }
            noticeService.save(new NoticeEntity(userEntity.getId(), NoticeType.GIFT_TO_REFERRAL, ResourceType.REFERRAL_BOX_1));
        }

        if (oldScore < 50 && newScore >= 50) {
            // если есть реферрер, то и ему начисляем очки
            if (userReferrer != null) {
                noticeService.save(new NoticeEntity(userReferrer.getId(), NoticeType.GIFT_TO_REFERRER, ResourceType.REFERRAL_BOX_2));
            }
            noticeService.save(new NoticeEntity(userEntity.getId(), NoticeType.GIFT_TO_REFERRAL, ResourceType.REFERRAL_BOX_2));
        }

        if (oldScore < 500 && newScore >= 500) {
            // если есть реферрер, то и ему начисляем очки
            if (userReferrer != null) {
                noticeService.save(new NoticeEntity(userReferrer.getId(), NoticeType.GIFT_TO_REFERRER, ResourceType.REFERRAL_BOX_3));
            }
            noticeService.save(new NoticeEntity(userEntity.getId(), NoticeType.GIFT_TO_REFERRAL, ResourceType.REFERRAL_BOX_3));
        }
    }

    @Transactional
    public void aggregateNotices() {
        int page = 0;
        int batchSize = 100;
        Page<Long> userIdsPage;

        do {
            // Получаем только ID пользователей
            userIdsPage = userService.findAllUserIds(
                    PageRequest.of(page, batchSize, Sort.by("id"))
            );
            List<Long> userIds = userIdsPage.getContent();
            List<Long> idsWithGifts = new ArrayList<>();

            // Награда приглашённым
            List<AggregateNoticeEntity> aggregateNoticeEntityForReferrals = noticeService.aggregateNotices(userIds, NoticeType.GIFT_TO_REFERRAL);
            //aggregateNoticeService.addTextAndTitleForProgress(aggregateNoticeEntityForReferrals);
            aggregateNoticeService.saveAll(aggregateNoticeEntityForReferrals);
            noticeService.updateNoticesStatus(userIds, NoticeType.GIFT_TO_REFERRAL, false, true);

            // Награда пригласившим
            List<AggregateNoticeEntity> aggregateNoticeEntityForReferrers = noticeService.aggregateNotices(userIds, NoticeType.GIFT_TO_REFERRER);
            //aggregateNoticeService.addTextAndTitleForReferrers(aggregateNoticeEntityForReferrers);
            aggregateNoticeService.saveAll(aggregateNoticeEntityForReferrers);
            noticeService.updateNoticesStatus(userIds, NoticeType.GIFT_TO_REFERRER, false, true);

            page++;
            log.info("Processed {} players for aggregateNotices", page * batchSize);
        } while (userIdsPage.hasNext());
    }

    private void grantReferralGifts(long referrerId, UserEntity userEntity) {
        UserEntity userReferrer = userService.findUserWithResourcesById(referrerId);
        if (userReferrer != null) {
            resourceService.addReferralBox1OrIncrement(userReferrer.getResources(), ResourceType.REFERRAL_BOX_1);

            userService.saveFullWithoutCache(userReferrer);
        }
        resourceService.addReferralBox1OrIncrement(userEntity.getResources(), ResourceType.REFERRAL_BOX_1);
    }

    public static void grantOldNotices(UserEntity userEntity, List<AggregateNoticeEntity> aggregateNotices) {
        boolean isOldHere = false;
        List<AggregateNoticeEntity> userNotices = new ArrayList<>();
        for (AggregateNoticeEntity aggregateNotice: aggregateNotices) {
            // Работа с каждым аггрегированным сообщением для данного пользователя
            if (aggregateNotice.getUserId().equals(userEntity.getId())) {
                if (!isOldHere) { isOldHere = compareWithLaterDate(aggregateNotice); }
                userNotices.add(aggregateNotice);
            }
        }
        if (!isOldHere) {
            userEntity.setSuperAggregate(false);
            return;
        }

        // ОБРАБОТКА СТАРЫХ СООБЩЕНИЙ
        for (AggregateNoticeEntity userNotice: userNotices) {
            /*
             * Это ресурсы для одного игрока в каждом отдельном уведомлении
             * Конвертируем в объекты Resource и добавляем ползователю методом addOrIncrementResource()
            */
            Map<String, Long> mapGrantedResources = userNotice.getResources();
            List<Resource> listGrantedResources = convertToResources(mapGrantedResources);

            // поиск по ресурсам одного пользователя
            Set<Resource> userResources = userEntity.getResources();
            addOrIncrementResource(userResources, listGrantedResources, userEntity.getId());
            // Отметка о том, что это сообщение больше не учитывать
            userNotice.setRead(true);
        }
        aggregateNoticeService.saveAll(userNotices);
        userEntity.setSuperAggregate(false);
    }

    private List<Long> chooseUserIds(List<UserEntity> userEntities) {
        List<Long> ids = new ArrayList<>();
        for (UserEntity userEntity : userEntities) {
            ids.add(userEntity.getId());
        }
        return ids;
    }

    public static List<Resource> convertToResources(Map<String, Long> grants) {
        List<Resource> resourceList = new ArrayList<>();
        for (String grant : grants.keySet()) {
            resourceList.add(ResourceList.createResource(grant, grants.get(grant)));
        }
        return resourceList;
    }

    public static void addOrIncrementResource(Set<Resource> userResources, List<Resource> grantedResources, Long userId) {
        for (Resource grant : grantedResources) {
            boolean found = false;
            for (Resource userResource : userResources) {
                if (userResource.getName().equals(grant.getName())) {
                    userResource.addQuantity(grant.getQuantity());
                    found = true;
                    break;
                }
            }
            if (!found) {
                // Создаем новый ресурс правильного типа
                grant.setUserId(userId);
                userResources.add(grant);
            }
        }
    }

    private static boolean compareWithLaterDate(AggregateNoticeEntity aggregateNotice) {
        // Если уведомление старое, то возвращаем true
        return aggregateNotice.getCreatedAt().getTime() < getLaterDate().getTime();
    }

    public void saveToSendMessageIfOldNoticesHere(UserEntity userEntity, List<AggregateNoticeEntity> aggregateNotices) {
        boolean isOldHere = false;
        for (AggregateNoticeEntity aggregateNotice: aggregateNotices) {
            // Работа с каждым аггрегированным сообщением для данного пользователя
            if (aggregateNotice.getUserId().equals(userEntity.getId())) {
                if (!isOldHere) { isOldHere = compareWithLaterDate(aggregateNotice); }
            }
        }
        if (!isOldHere) { return; }
        SuperAggregateEntity superAggregate = new SuperAggregateEntity(
                userEntity.getId(),
                userEntity.getTelegramId(),
                userEntity.getName(),
                NoticeType.SUPER_RESOURCES_GRANT);
        superAggregateService.save(superAggregate);

        // Добавляем пометку пользователю, чтобы потом прибавить к его ресурсам необходимые данные
        userEntity.setSuperAggregate(true);
    }

    public void sendSuperNotices() {
        SchedulerType schedulerType = SchedulerType.SEND_SUPER_MESSAGES;
        startSchedulerTracing(schedulerType);
        int page = 0;
        int batchSize = 100;
        Page<SuperAggregateEntity> aggregatePages;

        do {
            // Пагинация для обработки больших объемов данных
            aggregatePages = superAggregateService.findAllByPage(
                    PageRequest.of(page, batchSize, Sort.by("id"))
            );

            for (SuperAggregateEntity aggregate : aggregatePages.getContent()) {
                Response response = new SuperAggregateMessage(aggregate);
                response.initResponse();
            }
            superAggregateService.deleteAll(aggregatePages.getContent());

            page++;
            log.info("Processed {} players for sending super aggregate messages", page * batchSize);
        } while (aggregatePages.hasNext());
        endSchedulerTracing(schedulerType);
    }

    public static void grantForUser(UserDto userDto) {
        // Поиск всех непрочтённых аггрегированных уведомлений
        List<AggregateNoticeEntity> aggregateNotices = aggregateNoticeService.findGroupedNotices(List.of(userDto.getId()), false);
        if (aggregateNotices.isEmpty()) { return; }
        grantOldNotices(userDto, aggregateNotices);
    }

    public static void grantOldNotices(UserDto userDto, List<AggregateNoticeEntity> aggregateNotices) {
        boolean isOldHere = false;
        List<AggregateNoticeEntity> userNotices = new ArrayList<>();
        for (AggregateNoticeEntity aggregateNotice: aggregateNotices) {
            // Работа с каждым аггрегированным сообщением для данного пользователя
            if (aggregateNotice.getUserId().equals(userDto.getId())) {
                if (!isOldHere) { isOldHere = compareWithLaterDate(aggregateNotice); }
                userNotices.add(aggregateNotice);
            }
        }
        if (!isOldHere) {
            userDto.setSuperAggregate(false);
            return;
        }

        // ОБРАБОТКА СТАРЫХ СООБЩЕНИЙ
        for (AggregateNoticeEntity userNotice: userNotices) {
            /*
             * Это ресурсы для одного игрока в каждом отдельном уведомлении
             * Конвертируем в объекты Resource и добавляем ползователю методом addOrIncrementResource()
             */
            Map<String, Long> mapGrantedResources = userNotice.getResources();
            List<Resource> listGrantedResources = convertToResources(mapGrantedResources);

            // поиск по ресурсам одного пользователя
            List<Resource> userResources = userDto.getResources();
            addOrIncrementResource(userResources, listGrantedResources, userDto.getId());
            // Отметка о том, что это сообщение больше не учитывать
            userNotice.setRead(true);
        }
        aggregateNoticeService.saveAll(userNotices);
        userDto.setSuperAggregate(false);
    }

    public static void addOrIncrementResource(List<Resource> userResources, List<Resource> grantedResources, Long userId) {
        for (Resource grant : grantedResources) {
            boolean found = false;
            for (Resource userResource : userResources) {
                if (userResource.getName().equals(grant.getName())) {
                    userResource.addQuantity(grant.getQuantity());
                    found = true;
                    break;
                }
            }
            if (!found) {
                // Создаем новый ресурс правильного типа
                grant.setUserId(userId);
                userResources.add(grant);
            }
        }
    }
}