package com.creazione.space_learning.service.scheduler;

import com.creazione.space_learning.config.DataSet;
import com.creazione.space_learning.entities.game_entity.BuildingDto;
import com.creazione.space_learning.entities.game_entity.ResourceDto;
import com.creazione.space_learning.entities.game_entity.UserDto;
import com.creazione.space_learning.entities.postgres.*;
import com.creazione.space_learning.enums.NoticeType;
import com.creazione.space_learning.enums.SchedulerType;
import com.creazione.space_learning.game.resources.ResourceList;
import com.creazione.space_learning.enums.ResourceType;
import com.creazione.space_learning.queries.responces.Response;
import com.creazione.space_learning.queries.responces.SuperAggregateMessage;
import com.creazione.space_learning.service.*;
import com.creazione.space_learning.service.postgres.UserPostgresService;
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
    private static final UserPostgresService userService = DataSet.getUserService();
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
    private long calculateScore(List<BuildingDto> buildings) {
        return buildingService.getPointsForAllBuildings(buildings);
    }

    @Transactional
    public void scoresGrantsSuperAggregate() {
        SchedulerType schedulerType = SchedulerType.SUPER_AGGREGATE;
        startSchedulerTracing(schedulerType);

        int page = 0;
        int batchSize = 100;
        //Date cutoff = Date.from(Instant.now().minus(100, ChronoUnit.HOURS));
        Page<UserDto> usersPage;

        do {
            // Пагинация для обработки больших объемов данных
            usersPage = userService.findUsersWithResourcesAndRecentBuildingUpdates(
                    PageRequest.of(page, batchSize, Sort.by("id"))
            );
            System.out.println("Количество найденных пользователей: " + usersPage.getContent().size());


            List<Long> ids = chooseUserIds(usersPage.getContent());

            // Поиск всех непрочтённых аггрегированных уведомлений
            List<AggregateNoticeP> aggregateNotices = aggregateNoticeService.findGroupedNotices(ids, false);

            List<UserDto> userEntities = new ArrayList<>();
            for (UserDto userEntity : usersPage.getContent()) {
                if (userEntity == null || userEntity.getBuildings() == null) {continue;}
                List<BuildingDto> buildings = userEntity.getBuildings().stream().toList();
                resourceService.calculateQuantityChanges(userEntity, Instant.now());

                if (!aggregateNotices.isEmpty()) {
                    // Обработка всех аггрегированных уведомлений
                    if (userEntity.isSuperAggregate()) {
                        grantOldNotices(userEntity, aggregateNotices);
                    } else {
                        saveToSendMessageIfOldNoticesHere(userEntity, aggregateNotices);
                    }
                }

                PlayerScoreP playerScore = userEntity.getPlayerScore();

                long oldScore = 0;
                if (playerScore != null) {
                    oldScore = playerScore.getScore();
                } else {
                    playerScore = new PlayerScoreP(userEntity.getId());
                }
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
        SchedulerP scheduler = schedulerRepoService.findByType(type);
        scheduler = createSchedulerEntity(scheduler, type);
        schedulerRepoService.save(scheduler);
    }

    private void endSchedulerTracing(SchedulerType type) {
        SchedulerP scheduler = schedulerRepoService.findByType(type);
        closeSchedulerEntity(scheduler);
        schedulerRepoService.save(scheduler);
    }

    private SchedulerP createSchedulerEntity(SchedulerP scheduler, SchedulerType type) {
        if (scheduler == null) {
            SchedulerP schedulerFirst = new SchedulerP();
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

    private void closeSchedulerEntity(SchedulerP scheduler) {
        if (scheduler == null) { return; }
        Instant dateNow = Instant.now();
        long lastDuration = dateNow.toEpochMilli() - scheduler.getLastStart().toEpochMilli();
        scheduler.setRun(false);
        scheduler.setLastDuration(lastDuration);
        scheduler.setLastEnd(dateNow);
    }

    private void grantReferralGifts(UserDto userDto, long oldScore, long newScore) {
        Long referrerId = userDto.getReferrer();

        if (referrerId == null || referrerId == 0) {
            return;
        }

        UserDto userReferrer = userService.findBasicUserById(referrerId);
        if (oldScore < 5 && newScore >= 5) {
            // если есть реферрер, то и ему начисляем очки
            if (userReferrer != null) {
                noticeService.save(new NoticeP(userReferrer.getId(), NoticeType.GIFT_TO_REFERRER, ResourceType.REFERRAL_BOX_1));
            }
            noticeService.save(new NoticeP(userDto.getId(), NoticeType.GIFT_TO_REFERRAL, ResourceType.REFERRAL_BOX_1));
        }

        if (oldScore < 50 && newScore >= 50) {
            // если есть реферрер, то и ему начисляем очки
            if (userReferrer != null) {
                noticeService.save(new NoticeP(userReferrer.getId(), NoticeType.GIFT_TO_REFERRER, ResourceType.REFERRAL_BOX_2));
            }
            noticeService.save(new NoticeP(userDto.getId(), NoticeType.GIFT_TO_REFERRAL, ResourceType.REFERRAL_BOX_2));
        }

        if (oldScore < 500 && newScore >= 500) {
            // если есть реферрер, то и ему начисляем очки
            if (userReferrer != null) {
                noticeService.save(new NoticeP(userReferrer.getId(), NoticeType.GIFT_TO_REFERRER, ResourceType.REFERRAL_BOX_3));
            }
            noticeService.save(new NoticeP(userDto.getId(), NoticeType.GIFT_TO_REFERRAL, ResourceType.REFERRAL_BOX_3));
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
            List<AggregateNoticeP> aggregateNoticeEntityForReferrals = noticeService.aggregateNotices(userIds, NoticeType.GIFT_TO_REFERRAL);
            //aggregateNoticeService.addTextAndTitleForProgress(aggregateNoticeEntityForReferrals);
            aggregateNoticeService.saveAll(aggregateNoticeEntityForReferrals);
            noticeService.updateNoticesStatus(userIds, NoticeType.GIFT_TO_REFERRAL, false, true);

            // Награда пригласившим
            List<AggregateNoticeP> aggregateNoticeEntityForReferrers = noticeService.aggregateNotices(userIds, NoticeType.GIFT_TO_REFERRER);
            //aggregateNoticeService.addTextAndTitleForReferrers(aggregateNoticeEntityForReferrers);
            aggregateNoticeService.saveAll(aggregateNoticeEntityForReferrers);
            noticeService.updateNoticesStatus(userIds, NoticeType.GIFT_TO_REFERRER, false, true);

            page++;
            log.info("Processed {} players for aggregateNotices", page * batchSize);
        } while (userIdsPage.hasNext());
    }

    private void grantReferralGifts(long referrerId, UserDto userDto) {
        UserDto userReferrer = userService.findUserWithResourcesById(referrerId);
        if (userReferrer != null) {
            resourceService.addResourceRefBoxOrIncrement(userReferrer.getResources(), ResourceType.REFERRAL_BOX_1);

            userService.saveFullWithoutCache(userReferrer);
        }
        resourceService.addResourceRefBoxOrIncrement(userDto.getResources(), ResourceType.REFERRAL_BOX_1);
    }

    public static void grantOldNotices(UserDto userDto, List<AggregateNoticeP> aggregateNotices) {
        boolean isOldHere = false;
        List<AggregateNoticeP> userNotices = new ArrayList<>();
        for (AggregateNoticeP aggregateNotice: aggregateNotices) {
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
        for (AggregateNoticeP userNotice: userNotices) {
            /*
             * Это ресурсы для одного игрока в каждом отдельном уведомлении
             * Конвертируем в объекты Resource и добавляем ползователю методом addOrIncrementResource()
            */
            Map<String, Long> mapGrantedResources = userNotice.getResources();
            List<ResourceDto> listGrantedResources = convertToResources(mapGrantedResources);

            // поиск по ресурсам одного пользователя
            List<ResourceDto> userResources = userDto.getResources();
            addOrIncrementResource(userResources, listGrantedResources, userDto.getId());
            // Отметка о том, что это сообщение больше не учитывать
            userNotice.setRead(true);
        }
        aggregateNoticeService.saveAll(userNotices);
        userDto.setSuperAggregate(false);
    }

    private List<Long> chooseUserIds(List<UserDto> userDtos) {
        List<Long> ids = new ArrayList<>();
        for (UserDto userDto : userDtos) {
            ids.add(userDto.getId());
        }
        return ids;
    }

    public static List<ResourceDto> convertToResources(Map<String, Long> grants) {
        List<ResourceDto> resourceList = new ArrayList<>();
        for (String grant : grants.keySet()) {
            resourceList.add(ResourceList.createResource(grant, grants.get(grant)));
        }
        return resourceList;
    }

    public static void addOrIncrementResource(List<ResourceDto> userResources, List<ResourceDto> grantedResources, Long userId) {
        for (ResourceDto grant : grantedResources) {
            boolean found = false;
            for (ResourceDto userResource : userResources) {
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

    private static boolean compareWithLaterDate(AggregateNoticeP aggregateNotice) {
        // Если уведомление старое, то возвращаем true
        return aggregateNotice.getCreatedAt().getTime() < getLaterDate().getTime();
    }

    public void saveToSendMessageIfOldNoticesHere(UserDto userDto, List<AggregateNoticeP> aggregateNotices) {
        boolean isOldHere = false;
        for (AggregateNoticeP aggregateNotice: aggregateNotices) {
            // Работа с каждым аггрегированным сообщением для данного пользователя
            if (aggregateNotice.getUserId().equals(userDto.getId())) {
                if (!isOldHere) { isOldHere = compareWithLaterDate(aggregateNotice); }
            }
        }
        if (!isOldHere) { return; }
        SuperAggregateP superAggregate = new SuperAggregateP(
                userDto.getId(),
                userDto.getTelegramId(),
                userDto.getName(),
                NoticeType.SUPER_RESOURCES_GRANT);
        superAggregateService.save(superAggregate);

        // Добавляем пометку пользователю, чтобы потом прибавить к его ресурсам необходимые данные
        userDto.setSuperAggregate(true);
    }

    public void sendSuperNotices() {
        SchedulerType schedulerType = SchedulerType.SEND_SUPER_MESSAGES;
        startSchedulerTracing(schedulerType);
        int page = 0;
        int batchSize = 100;
        Page<SuperAggregateP> aggregatePages;

        do {
            // Пагинация для обработки больших объемов данных
            aggregatePages = superAggregateService.findAllByPage(
                    PageRequest.of(page, batchSize, Sort.by("id"))
            );

            for (SuperAggregateP aggregate : aggregatePages.getContent()) {
                SuperAggregateMessage response = new SuperAggregateMessage();
                response.initResponse(aggregate);
            }
            superAggregateService.deleteAll(aggregatePages.getContent());

            page++;
            log.info("Processed {} players for sending super aggregate messages", page * batchSize);
        } while (aggregatePages.hasNext());
        endSchedulerTracing(schedulerType);
    }

    public static void grantForUser(UserDto userDto) {
        // Поиск всех непрочтённых аггрегированных уведомлений
        List<AggregateNoticeP> aggregateNotices = aggregateNoticeService.findGroupedNotices(List.of(userDto.getId()), false);
        if (aggregateNotices.isEmpty()) { return; }
        grantOldNotices(userDto, aggregateNotices);
    }
}