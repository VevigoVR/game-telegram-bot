package com.creazione.space_learning.service;

import com.creazione.space_learning.entities.postgres.AggregateNoticeP;
import com.creazione.space_learning.entities.postgres.NoticeP;
import com.creazione.space_learning.enums.NoticeType;
import com.creazione.space_learning.enums.ResourceType;
import com.creazione.space_learning.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NoticeService {
    private final NoticeRepository noticeRepository;

    public NoticeP save(NoticeP noticeEntity) {
        return noticeRepository.save(noticeEntity);
    }

    public List<NoticeP> findAllByUserId(List<Long> ids) {
        return noticeRepository.findAllByUserIdIn(ids);
    }

    public List<AggregateNoticeP> aggregateNotices(
            List<Long> userIds,
            NoticeType noticeType
    ) {
        List<Object[]> groupedData = noticeRepository.findGroupedNotices(userIds, noticeType);

        // Группировка по userId
        Map<Long, AggregateNoticeP> resultMap = new HashMap<>();

        for (Object[] row : groupedData) {
            Long userId = (Long) row[0];
            ResourceType resourceType = (ResourceType) row[2];
            Long count = (Long) row[3];

            AggregateNoticeP aggregate = resultMap.computeIfAbsent(
                    userId,
                    id -> new AggregateNoticeP(userId, noticeType, 0)
            );

            // Обновляем общее количество
            aggregate.setQuantity(aggregate.getQuantity() + count);

            // Добавляем ресурс в карту
            String resourceName = resourceType.toString();
            aggregate.getResources().merge(resourceName, count, Long::sum);
        }

        return new ArrayList<>(resultMap.values());
    }

    public int updateNoticesStatus(List<Long> userIds, NoticeType type, boolean wasRead, boolean nowRead) {
        return noticeRepository.updateNoticesStatus(userIds, type, wasRead, nowRead);
    }
}
