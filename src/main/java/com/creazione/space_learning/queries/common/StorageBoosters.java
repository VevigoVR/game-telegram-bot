package com.creazione.space_learning.queries.common;

import com.creazione.space_learning.entities.postgres.InventoryBoosterP;
import com.creazione.space_learning.queries.GameCommand;
import com.creazione.space_learning.queries.Query;
import com.creazione.space_learning.utils.Answer;
import com.creazione.space_learning.enums.Emoji;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Component
@GameCommand(
        value = {"/boosters", "бустеры", "/boostersnw"},
        description = "Просмотр склада/ресурсов игрока"
)
public class StorageBoosters extends Query {
    public StorageBoosters() {
        super(List.of());
    }

    @Override
    public Answer respond(Update update) {
        Answer answer = new Answer();
        initialQuery(update, true);

        if (!isStatus()) {
            SendMessage sendMessage = getSendMessageFalse();
            answer.setSendMessage(sendMessage);
            return answer;
        }

        if (update.hasCallbackQuery()) {
            answer.setAnswerCallbackQuery(closeRespond(update));
            if (update.getCallbackQuery().getData().equals("/boostersnw")) {
                answer.setSendPhoto(getSendPhoto());
                return answer;
            }
            EditMessageCaption newText = EditMessageCaption.builder()
                    .chatId(getChatId())
                    .messageId(getMessageId())
                    .build();
            newText.setReplyMarkup(getInlineKeyboardMarkup());
            newText.setCaption(getText());
            newText.setParseMode(ParseMode.HTML);
            answer.setEditMessageCaption(newText);
        } else {
            answer.setSendPhoto(getSendPhoto());
        }
        return answer;
    }

    @Override
    public SendPhoto getSendPhoto() {
        String img = getImg();
        String text = getText();
        SendPhoto message = sendCustomPhoto(getChatId(), img, getTargetImg(), text);
        message.setReplyMarkup(getInlineKeyboardMarkup());
        return message;
    }

    @Override
    public String getText() {
        StringBuilder text = new StringBuilder();
        text.append("<b>Склад ").append(getUserName()).append("</b>\n\n<b>Ускорители</b>:\n");
        if (!getUserDto().viewSortedBoosters().isEmpty()) {
            for (InventoryBoosterP booster : getUserDto().viewSortedBoosters()) {
                text.append(booster.getName().getEmoji()).append(" <b>").append(booster.getName())
                        .append("</b> на ").append((int) (booster.getValue()*100)).append("% от базового значения")
                        .append(" в течение ").append(booster.getDurationMilli()/3600000).append(" часов")
                        .append(": ")
                        .append(booster.makeQuantityString())
                        .append(" шт.\n")
                        .append("<b>активировать: </b><code>")
                        .append(booster.getName().getMark()).append(" ")
                        .append((int) (booster.getValue()*100)).append(" ")
                        .append(booster.getDurationMilli()/3600000).append("</code>\n");
            }
        } else {
            text.append("<i>на складе не найдено ускорителей...</i>\n");
        }

        text.append(getSpoiler());
        return text.toString();
    }

    @Override
    public InlineKeyboardMarkup getInlineKeyboardMarkup() {
        List<Integer> buttonsInLine = List.of(2, 3);
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        buttons.add(getButton(Emoji.HOUSE.toString(), "/profile"));
        buttons.add((getButton(Emoji.EJECT_SYMBOL.toString(), "/boostersnw")));
        buttons.add(getButton("Ресурсы", "/resources"));
        buttons.add(getButton("Коробки", "/lootboxes"));
        buttons.add(getButton(Emoji.ARROWS_COUNTERCLOCKWISE.toString(), "/boosters"));
        return getKeyboard(buttonsInLine, buttons);
    }
}