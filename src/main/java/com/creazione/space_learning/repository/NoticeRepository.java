package com.creazione.space_learning.repository;

import com.creazione.space_learning.entities.NoticeEntity;
import com.creazione.space_learning.enums.NoticeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
// JDBC Template, Jooq
@Repository
public interface NoticeRepository extends JpaRepository<NoticeEntity, Long> {
    List<NoticeEntity> findAllByUserIdIn(List<Long> ids);

    /*
    @Query("SELECT NEW com.creazione.space_learning.entities.AggregateNoticeEntity(" +
            "n.userId, n.noticeType, COUNT(n)) " +
            "FROM NoticeEntity n " +
            "WHERE n.userId IN :userIds " +
            "AND n.noticeType = :noticeType " +
            "AND n.read = false " +
            "GROUP BY n.userId, n.noticeType")
    List<AggregateNoticeEntity> aggregateNotices(
            @Param("userIds") List<Long> userIds,
            @Param("noticeType") NoticeType noticeType
    );
     */

    @Query("SELECT n.userId, n.noticeType, n.resourceType, COUNT(n) " +
            "FROM NoticeEntity n " +
            "WHERE n.userId IN :userIds AND n.noticeType = :noticeType " +
            "GROUP BY n.userId, n.noticeType, n.resourceType")
    List<Object[]> findGroupedNotices(
            @Param("userIds") List<Long> userIds,
            @Param("noticeType") NoticeType noticeType
    );

    /**
    * Обновляет поле notices у пользователей по списку ID
    *
    * @param ids Список ID пользователей
    * @return Количество обновленных записей
    */
    @Transactional
    @Modifying
    @Query("UPDATE NoticeEntity n SET n.isRead = :isRead WHERE n.userId IN :ids AND n.isRead = :wasIsRead AND n.noticeType = :noticeType")
    int updateNoticesStatus(@Param("ids") List<Long> ids,
                            @Param("noticeType") NoticeType noticeType,
                            @Param("wasIsRead") boolean wasIsRead,
                            @Param("isRead") boolean isRead);
}
