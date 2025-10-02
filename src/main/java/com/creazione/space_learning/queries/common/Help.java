package com.creazione.space_learning.queries.common;

import com.creazione.space_learning.dto.UserInitialDto;
import com.creazione.space_learning.entities.game_entity.UserDto;
import com.creazione.space_learning.queries.GameCommand;
import com.creazione.space_learning.queries.Query;
import com.creazione.space_learning.utils.Answer;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;

@Component
@GameCommand(
        value = {"/help", "помощь", "справка", ".help", ".помощь"},
        description = "Помощь по командам"
)
public class Help extends Query {
    public Help() {
        super(List.of());
    }

    @Override
    public Answer respond(Update update) {
        long chatId;
        Answer answer = new Answer();
        if (update.hasCallbackQuery()) {
            answer.setAnswerCallbackQuery(closeRespond(update));
            chatId = update.getCallbackQuery().getMessage().getChatId();
            //setUserName(update.getCallbackQuery().getFrom().getUserName());
            String text = """
                <b>Помощь по вселенной Creazione</b> \n
                Начать путешествие: /start
                """;
            answer.setSendMessage(sendCustomMessage(chatId, text));

        } else {
            chatId = update.getMessage().getChatId();
            //setUserName(update.getMessage().getChat().getUserName());
            String text = """
                <b>Помощь по вселенной Creazione</b> \n
                Начать путешествие: /start
                """;
            answer.setSendMessage(sendCustomMessage(chatId, text));
        }



        return answer;
    }

    @Override
    public InlineKeyboardMarkup getInlineKeyboardMarkup() {
        return null;
    }

    @Override
    public String getText(UserDto userDto) {
        return "";
    }

    @Override
    public SendPhoto getSendPhoto(UserInitialDto userInitialDto) {
        return null;
    }
}
