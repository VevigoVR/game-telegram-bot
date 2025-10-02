package com.creazione.space_learning.queries.responces;

import com.creazione.space_learning.enums.Emoji;
import lombok.Getter;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Getter
@Setter
public class MaintenanceMessage extends Response<Long> {
    //private long telegramId;

    public MaintenanceMessage() {
        super();
    }

    @Override
    public void initResponse(Long telegramId) {
        createResponse(answer -> answer.setSendMessage(constructSendMessage(telegramId)));
    }

    private SendMessage constructSendMessage(long telegramId) {
        return createCustomMessage(telegramId, createMessageText());
    }

    private String createMessageText() {
        return Emoji.STELLITE + " Сервис находится на техническом обслуживании.\n" +
                "Пожалуйста, попробуйте позже. Обычно обслуживание завершается к 4:00.\n";
    }
}
