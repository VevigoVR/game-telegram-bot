package com.creazione.space_learning.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TradeNpcRequestDto {
    private String quantityOfResource = "";
    private String wrong = "";
    private TransferTradeResult tradeResult;
}
