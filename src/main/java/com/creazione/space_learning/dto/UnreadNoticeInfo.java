package com.creazione.space_learning.dto;

import com.creazione.space_learning.entities.AggregateNoticeEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class UnreadNoticeInfo {
    private AggregateNoticeEntity latestNotice;
    private boolean hasMoreUnread;

}