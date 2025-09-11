package com.creazione.space_learning.queries.common;

import com.creazione.space_learning.config.DataSet;
import com.creazione.space_learning.dto.MessageText;
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
    private MessageText wrong = new MessageText();
    private List<Item> items; // если wrong.text пустой, то items может быть null
    public DailyGift() {
        super(List.of());
    }

    @Override
    public Answer respond(Update update) {
        wrong.setText("");

        Answer answer = new Answer();
        initialQuery(update, false);

        if (!isStatus()) {
            SendMessage sendMessage = getSendMessageFalse();
            answer.setSendMessage(sendMessage);
            return answer;
        }

        if (update.hasCallbackQuery()) {
            answer.setAnswerCallbackQuery(closeRespond(update));
        }

        takeDailyGift();

        SendMessage sendMessage = takeSendMessage();
        answer.setSendMessage(sendMessage);
        return answer;
    }

    private SendMessage takeSendMessage() {
        return sendCustomMessage(getChatId(), getText());
    }

    @Override
    public SendPhoto getSendPhoto() {
        return null;
    }

    @Override
    public String getText() {
        if (!wrong.getText().isEmpty()) {
            return wrong.getText();
        } else {
            return "✅ <b>Поздравляем! Вы получили подарок:</b>\n" + getGiftToString();
        }
    }

    private String getGiftToString() {
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

    private void takeDailyGift() {
        items = DataSet.getDailyGiftService().takeDailyGift(getUserDto(), wrong);
    }
}
