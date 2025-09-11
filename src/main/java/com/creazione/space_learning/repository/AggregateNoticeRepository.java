package com.creazione.space_learning.repository;

import com.creazione.space_learning.entities.AggregateNoticeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AggregateNoticeRepository extends JpaRepository<AggregateNoticeEntity, Long> {
    @Query("SELECT DISTINCT anr FROM AggregateNoticeEntity anr " +
            "WHERE anr.userId IN :userIds AND anr.isRead = :isRead")
    List<AggregateNoticeEntity> findGroupedNotices(
            @Param("userIds") List<Long> userIds,
            @Param("isRead") boolean isRead
    );

    @Query("SELECT DISTINCT a FROM AggregateNoticeEntity a " +
            "WHERE a.userId = :id AND a.isRead = false ORDER BY a.createdAt DESC")
    List<AggregateNoticeEntity> findGroupedNoticesByIdAndIsReadFalse(@Param("id") Long id);

    @Query("SELECT DISTINCT a FROM AggregateNoticeEntity a " +
            "WHERE a.userId = :id AND a.isRead = false ORDER BY a.createdAt DESC")
    Page<AggregateNoticeEntity> findLatestUnreadNotice(@Param("id") Long id, Pageable pageable);

    // Метод-обертка для удобства использования
    default AggregateNoticeEntity findLatestUnreadNoticeByUserId(Long userId) {
        Page<AggregateNoticeEntity> page = findLatestUnreadNotice(userId, PageRequest.of(0, 1));
        return page.hasContent() ? page.getContent().get(0) : null;
    }

    AggregateNoticeEntity findFirstByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);
    long countByUserIdAndIsReadFalse(Long userId);
}