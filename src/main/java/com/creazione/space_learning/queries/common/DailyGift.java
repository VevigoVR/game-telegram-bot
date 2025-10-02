package com.creazione.space_learning.queries.common;

import com.creazione.space_learning.config.DataSet;
import com.creazione.space_learning.dto.MessageText;
import com.creazione.space_learning.dto.UserInitialDto;
import com.creazione.space_learning.entities.game_entity.UserDto;
import com.creazione.space_learning.game.Item;
import com.creazione.space_learning.queries.GameCommand;
import com.creazione.space_learning.queries.Query;
import com.creazione.space_learning.utils.Answer;
import com.creazione.space_learning.utils.Formatting;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Component
@GameCommand(
        value = {"/gift", "бонус", "подарок"},
        description = "Получить ежедневный подарок"
)
public class DailyGift extends Query {
    //private List<Item> items; // если wrong.text пустой, то items может быть null
    public DailyGift() {
        super(List.of());
    }

    @Override
    public Answer respond(Update update) {
        MessageText messageText = new MessageText();

        Answer answer = new Answer();
        UserInitialDto userInitialDto = initialQuery(update, false);

        if (!userInitialDto.isStatus()) {
            SendMessage sendMessage = getSendMessageFalse(userInitialDto.getChatId());
            answer.setSendMessage(sendMessage);
            return answer;
        }

        if (update.hasCallbackQuery()) {
            answer.setAnswerCallbackQuery(closeRespond(update));
        }

        messageText.setItems(takeDailyGift(userInitialDto.getUserDto(), messageText));

        SendMessage sendMessage = takeSendMessage(userInitialDto, messageText);
        answer.setSendMessage(sendMessage);
        return answer;
    }

    private SendMessage takeSendMessage(UserInitialDto userInitialDto, MessageText messageText) {
        return sendCustomMessage(userInitialDto.getChatId(), getText(userInitialDto.getUserDto(), messageText));
    }

    @Override
    public SendPhoto getSendPhoto(UserInitialDto userInitialDto) {
        return null;
    }

    @Override
    public String getText(UserDto userDto, MessageText messageText) {
        if (!messageText.getText().isEmpty()) {
            return messageText.getText();
        } else {
            return "✅ <b>Поздравляем! Вы получили подарок:</b>\n" + getGiftToString(messageText.getItems());
        }
    }

    private String getGiftToString(List<Item> items) {
        StringBuilder itemsToString = new StringBuilder();
        for (Item item : items) {
            itemsToString.append(item.getName().getEmoji()).append(" ")
                    .append(item.getName().getName()).append(": +")
                    .append(Formatting.formatWithDots(item.getQuantity()))
                    .append("\n");
        }

        return itemsToString.toString();
    }

    @Override
    public InlineKeyboardMarkup getInlineKeyboardMarkup() {
        List<Integer> buttonsInLine = List.of(1);
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        return getKeyboard(buttonsInLine, buttons);
    }

    private List<Item> takeDailyGift(UserDto userDto, MessageText messageText) {
        return DataSet.getDailyGiftService().takeDailyGift(userDto, messageText);
    }
}
