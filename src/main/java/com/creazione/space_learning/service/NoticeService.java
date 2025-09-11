package com.creazione.space_learning.service;

import com.creazione.space_learning.entities.AggregateNoticeEntity;
import com.creazione.space_learning.entities.NoticeEntity;
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

    public NoticeEntity save(NoticeEntity noticeEntity) {
        return noticeRepository.save(noticeEntity);
    }

    public List<NoticeEntity> findAllByUserId(List<Long> ids) {
        return noticeRepository.findAllByUserIdIn(ids);
    }

    public List<AggregateNoticeEntity> aggregateNotices(
            List<Long> userIds,
            NoticeType noticeType
    ) {
        List<Object[]> groupedData = noticeRepository.findGroupedNotices(userIds, noticeType);

        // Группировка по userId
        Map<Long, AggregateNoticeEntity> resultMap = new HashMap<>();

        for (Object[] row : groupedData) {
            Long userId = (Long) row[0];
            ResourceType resourceType = (ResourceType) row[2];
            Long count = (Long) row[3];

            AggregateNoticeEntity aggregate = resultMap.computeIfAbsent(
                    userId,
                    id -> new AggregateNoticeEntity(userId, noticeType, 0)
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
