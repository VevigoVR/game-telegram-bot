package com.creazione.space_learning.queries.responces;

import com.creazione.space_learning.enums.Emoji;
import lombok.Getter;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Getter
@Setter
public class MaintenanceMessage extends Response {
    //private long telegramId;

    public MaintenanceMessage(long telegramId) {
        super();
        this.telegramId = telegramId;
    }

    @Override
    public void initResponse() {
        createResponse(answer -> answer.setSendMessage(constructSendMessage()));
    }

    private SendMessage constructSendMessage() {
        return createCustomMessage(getTelegramId(), createMessageText());
    }

    private String createMessageText() {
        return Emoji.STELLITE + " Сервис находится на техническом обслуживании.\n" +
                "Пожалуйста, попробуйте позже. Обычно обслуживание завершается к 4:00.\n";
    }
}
