package com.creazione.space_learning.entities.postgres;

import com.creazione.space_learning.enums.NoticeType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
/*
*   Класс уведомления пользователя о событии,
*   уже используется для отправки уведомлений в чат
 *  через scheduler.
 */
@Entity
@Table(name = "super_aggregate")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SuperAggregateP {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private Long telegramId;
    private String userName;
    @Enumerated(EnumType.STRING)
    private NoticeType noticeType;
    private boolean isSend = false;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;

    public SuperAggregateP(Long userId, Long telegramId, String userName, NoticeType type) {
        this.userId = userId;
        this.telegramId = telegramId;
        this.userName = userName;
        this.noticeType = type;
        this.isSend = false;
        this.createdAt = new Date();
    }
}