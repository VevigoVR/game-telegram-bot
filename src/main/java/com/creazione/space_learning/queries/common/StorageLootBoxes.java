package com.creazione.space_learning.queries.common;

import com.creazione.space_learning.entities.Resource;
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
        value = {"/lootboxes", "коробки", "/lootboxesnw"},
        description = "Просмотр склада/ресурсов игрока"
)
public class StorageLootBoxes extends Query {
    public StorageLootBoxes() {
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
            if (update.getCallbackQuery().getData().equals("/lootboxesnw")) {
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
        text.append("<b>Склад ").append(getUserName()).append("</b>\n\n<b>Коробки</b>:\n");
        if (!getUserDto().viewSortedLootBoxes().isEmpty()) {
            for (Resource resource : getUserDto().viewSortedLootBoxes()) {
                text.append(resource.getEmoji()).append(" ").append(resource.getName()).append(": ")
                        .append(resource.makeQuantityString())
                        .append(" шт.\n")
                        .append("<b>активировать: </b><code>/open ")
                        .append(resource.getName().getMark())
                        .append("</code>\n");
            }
        } else {
            text.append("""
                    <i>на складе не найдено коробок...</i>

                    Но вы всегда можете получить ежедневный бонус!
                    Используйте команду <code>/gift</code> 🎁
                    """);
        }

        text.append(getSpoiler());
        return text.toString();
    }

    @Override
    public InlineKeyboardMarkup getInlineKeyboardMarkup() {
        List<Integer> buttonsInLine = List.of(2, 3);
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        buttons.add(getButton(Emoji.HOUSE.toString(), "/profile"));
        buttons.add((getButton(Emoji.EJECT_SYMBOL.toString(), "/lootboxesnw")));
        buttons.add(getButton("Ресурсы", "/resources"));
        buttons.add(getButton(Emoji.ARROWS_COUNTERCLOCKWISE.toString(), "/lootboxes"));
        buttons.add(getButton("Ускорители", "/boosters"));
        return getKeyboard(buttonsInLine, buttons);
    }
}