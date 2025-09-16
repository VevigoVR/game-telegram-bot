package com.creazione.space_learning.entities.postgres;

import com.creazione.space_learning.enums.NoticeType;
import com.creazione.space_learning.enums.ResourceType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Entity
@Table(name = "notices")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NoticeP {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;

    @Enumerated(EnumType.STRING)
    private NoticeType noticeType;
    @Enumerated(EnumType.STRING)
    private ResourceType resourceType;

    private boolean isRead = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt = new Date();

    public NoticeP(Long userId, NoticeType noticeType, ResourceType resourceType) {
        this.userId = userId;
        this.noticeType = noticeType;
        this.resourceType = resourceType;
    }
}
