package com.creazione.space_learning.service;

import com.creazione.space_learning.dto.UnreadNoticeInfo;
import com.creazione.space_learning.entities.AggregateNoticeEntity;
import com.creazione.space_learning.game.resources.ReferralBox1;
import com.creazione.space_learning.repository.AggregateNoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AggregateNoticeService {
    private final AggregateNoticeRepository aggregateNoticeRepository;

    public void saveAll(List<AggregateNoticeEntity> aggregateNoticeEntities) {
        aggregateNoticeRepository.saveAll(aggregateNoticeEntities);
    }

    public void addTextAndTitleForReferrers(List<AggregateNoticeEntity> aggregateNoticeEntities) {
        for (AggregateNoticeEntity notice : aggregateNoticeEntities) {
            ReferralBox1 referralBox1 = new ReferralBox1(notice.getQuantity());
            notice.setTitle("Награда за активных рефералов");
            notice.setText("Поздравляем! За участие и активность Ваших приглашённых соратников Вы получаете:\n" +
                    referralBox1.getEmoji() + " " + referralBox1.getName() + ": " + referralBox1.getQuantity() + " шт.");
        }
    }

    public void addTextAndTitleForProgress(List<AggregateNoticeEntity> aggregateNoticeEntities) {
        for (AggregateNoticeEntity notice : aggregateNoticeEntities) {
            ReferralBox1 referralBox1 = new ReferralBox1(notice.getQuantity());
            notice.setTitle("Награда за активность в команде");
            notice.setText("Поздравляем! За участие и активность в команде с человеком пригласившим Вас, Вы получаете:\n" +
                    referralBox1.getEmoji() + " " + referralBox1.getName() + ": " + referralBox1.getQuantity() + " шт.");
        }
    }

    public List<AggregateNoticeEntity> findGroupedNotices(List<Long> ids, boolean isRead) {
        return aggregateNoticeRepository.findGroupedNotices(ids, isRead);
    }

    public List<AggregateNoticeEntity> findGroupedNoticesByIdAndIsReadFalse(Long id) {
        return aggregateNoticeRepository.findGroupedNoticesByIdAndIsReadFalse(id);
    }

    public void deleteAll() {
        aggregateNoticeRepository.deleteAll();
    }

    public AggregateNoticeEntity findFirstByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long id) {
        return aggregateNoticeRepository.findFirstByUserIdAndIsReadFalseOrderByCreatedAtDesc(id);
    }

    public void save(AggregateNoticeEntity aggregateNotice) {
        aggregateNoticeRepository.save(aggregateNotice);
    }
/*
    public UnreadNoticeInfo findLatestUnreadNoticeWithHasMoreFlag(long id) {
        Object[] result = aggregateNoticeRepository.findLatestUnreadNoticeWithHasMoreFlag(id);
        if (result == null || result.length == 0) {
            return new UnreadNoticeInfo(null, false);
        }

        // Первый элемент - это уведомление (AggregateNoticeEntity)
        AggregateNoticeEntity notice = (AggregateNoticeEntity) result[0];

        // Второй элемент - флаг has_more (Boolean)
        boolean hasMore = (Boolean) result[1];

        return new UnreadNoticeInfo(notice, hasMore);
    }

 */

    public UnreadNoticeInfo findLatestUnreadNoticeWithHasMoreFlag(Long userId) {
        // Получаем последнее уведомление
        AggregateNoticeEntity notice = aggregateNoticeRepository.findFirstByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);

        if (notice == null) {
            return new UnreadNoticeInfo(null, false);
        }

        // Проверяем, есть ли другие непрочитанные уведомления
        long count = aggregateNoticeRepository.countByUserIdAndIsReadFalse(userId);
        boolean hasMore = count > 1;

        return new UnreadNoticeInfo(notice, hasMore);
    }
}
