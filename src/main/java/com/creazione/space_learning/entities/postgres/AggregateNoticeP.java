package com.creazione.space_learning.entities.postgres;

import com.creazione.space_learning.enums.NoticeType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.vladmihalcea.hibernate.type.json.JsonType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.Parameter;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "aggregate_notes")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AggregateNoticeP {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;

    @Enumerated(EnumType.STRING)
    private NoticeType noticeType;

    @Type(value = JsonType.class, parameters = @Parameter(name = "classType", value = "java.util.Map"))
    @Column(columnDefinition = "jsonb")
    private Map<String, Long> resources = new HashMap<>();

    private String title = "";
    private String text = "";
    private long quantity;
    private boolean isRead = false;

    //@CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;

    public AggregateNoticeP(Long userId, NoticeType type, long quantity) {
        this.userId = userId;
        this.noticeType = type;
        this.quantity = quantity;
        this.isRead = false;
    }
}