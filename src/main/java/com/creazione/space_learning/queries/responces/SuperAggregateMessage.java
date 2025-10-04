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
public class SuperAggregateMessage extends Response<SuperAggregateP> {
    //private SuperAggregateP superAggregateEntity;

    public SuperAggregateMessage() {
        super();
    }

    @Override
    public void initResponse(SuperAggregateP superAggregateEntity) {
        createResponse(answer -> answer.setSendMessage(constructSendMessage(superAggregateEntity)));
    }

    private SendMessage constructSendMessage(SuperAggregateP superAggregateEntity) {
        SendMessage message = createCustomMessage(superAggregateEntity.getTelegramId(), createMessageText(superAggregateEntity));
        message.setReplyMarkup(getInlineKeyboardMarkup());
        return message;
    }

    private String createMessageText(SuperAggregateP superAggregateEntity) {
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