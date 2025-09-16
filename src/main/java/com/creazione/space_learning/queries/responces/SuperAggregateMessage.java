package com.creazione.space_learning.queries.responces;

import com.creazione.space_learning.entities.postgres.SuperAggregateP;
import com.creazione.space_learning.enums.Emoji;
import lombok.Getter;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SuperAggregateMessage extends Response {
    private SuperAggregateP superAggregateEntity;

    public SuperAggregateMessage(SuperAggregateP superAggregateEntity) {
        super();
        this.superAggregateEntity = superAggregateEntity;
    }

    @Override
    public void initResponse() {
        createResponse(answer -> answer.setSendMessage(constructSendMessage()));
    }

    private SendMessage constructSendMessage() {
        SendMessage message = createCustomMessage(superAggregateEntity.getTelegramId(), createMessageText());
        message.setReplyMarkup(getInlineKeyboardMarkup());
        return message;
    }

    private String createMessageText() {
        return Emoji.MAN_ASTRONAUT + "Привет, " + superAggregateEntity.getUserName() + "! Ты давно не заходил в свой Центр Сообщений!\n\n" +
                "Потому...\n\n" +
                "✅ Письма сохранить не удалось, но посылки успешно доставлены на склад!\n\n";
    }

    private InlineKeyboardMarkup getInlineKeyboardMarkup() {
        List<Integer> buttonsInLine = List.of(1);
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        buttons.add(getButton("Склад", "/resourcesnw"));
        return getKeyboard(buttonsInLine, buttons);
    }
}