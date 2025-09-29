package com.creazione.space_learning.dto;

import com.creazione.space_learning.entities.game_entity.ResourceDto;
import com.creazione.space_learning.enums.ResourceType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransferTradeResult {
    private String message;
    private boolean transferred;
    private long amount;
    private ResourceDto userResource;
    private ResourceDto npcResource;
    private boolean buy;
    
    public TransferTradeResult (String message) {
        this.message = message;
        this.transferred = false;
    }
}