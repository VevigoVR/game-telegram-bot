package com.creazione.space_learning.queries.responces;

import com.creazione.space_learning.config.DataSet;
import com.creazione.space_learning.utils.Answer;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class Response {

    protected SendMessage createCustomMessage(Long chatId, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setParseMode(ParseMode.HTML);
        sendMessage.disableWebPagePreview();
        sendMessage.setChatId(chatId);
        sendMessage.enableHtml(true);
        sendMessage.setText(text);
        return sendMessage;
    }

    protected InlineKeyboardButton getButton(String text, String data) {
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButton.setText(text);
        inlineKeyboardButton.setCallbackData(data);
        return inlineKeyboardButton;
    }

    protected InlineKeyboardMarkup getKeyboard(List<Integer> rowsInLine, List<InlineKeyboardButton> buttons) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        int key = 0;
        for (int i = 0; i < rowsInLine.size(); i++) {
            List<InlineKeyboardButton> buttonsInLine = new ArrayList<>();
            for (int row = 0; row < rowsInLine.get(i); row++) {
                buttonsInLine.add(buttons.get(key));
                key++;
            }
            rowsInline.add(buttonsInLine);
        }
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    protected void createResponse(Consumer<Answer> setter) {
        Answer answer = new Answer();
        setter.accept(answer);
        DataSet.getThrottledSender4().enqueueMessage(answer);
    }

    public abstract void initResponse();
}
