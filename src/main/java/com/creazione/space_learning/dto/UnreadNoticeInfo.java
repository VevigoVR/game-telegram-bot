package com.creazione.space_learning.dto;

import com.creazione.space_learning.entities.postgres.AggregateNoticeP;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class UnreadNoticeInfo {
    private AggregateNoticeP latestNotice;
    private boolean hasMoreUnread;
}