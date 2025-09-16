package com.creazione.space_learning.repository;

import com.creazione.space_learning.entities.postgres.AggregateNoticeP;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AggregateNoticeRepository extends JpaRepository<AggregateNoticeP, Long> {
    @Query("SELECT DISTINCT anr FROM AggregateNoticeP anr " +
            "WHERE anr.userId IN :userIds AND anr.isRead = :isRead")
    List<AggregateNoticeP> findGroupedNotices(
            @Param("userIds") List<Long> userIds,
            @Param("isRead") boolean isRead
    );

    @Query("SELECT DISTINCT a FROM AggregateNoticeP a " +
            "WHERE a.userId = :id AND a.isRead = false ORDER BY a.createdAt DESC")
    List<AggregateNoticeP> findGroupedNoticesByIdAndIsReadFalse(@Param("id") Long id);

    @Query("SELECT DISTINCT a FROM AggregateNoticeP a " +
            "WHERE a.userId = :id AND a.isRead = false ORDER BY a.createdAt DESC")
    Page<AggregateNoticeP> findLatestUnreadNotice(@Param("id") Long id, Pageable pageable);

    // Метод-обертка для удобства использования
    default AggregateNoticeP findLatestUnreadNoticeByUserId(Long userId) {
        Page<AggregateNoticeP> page = findLatestUnreadNotice(userId, PageRequest.of(0, 1));
        return page.hasContent() ? page.getContent().get(0) : null;
    }

    AggregateNoticeP findFirstByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);
    long countByUserIdAndIsReadFalse(Long userId);
}