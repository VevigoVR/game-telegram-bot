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
public class Help extends Query<Object> {
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
            answer.setSendMessage(sendCustomMessage(chatId, getText(null, null)));

        } else {
            chatId = update.getMessage().getChatId();
            answer.setSendMessage(sendCustomMessage(chatId, getText(null, null)));
        }



        return answer;
    }

    @Override
    public InlineKeyboardMarkup getInlineKeyboardMarkup(UserInitialDto userInitialDto, Object object) {
        return null;
    }

    @Override
    public String getText(UserInitialDto userInitialDto, Object object) {
        return  """
                <b>Помощь по вселенной Creazione</b>
                
                Начать путешествие: /start
                """;
    }

    @Override
    public SendPhoto getSendPhoto(UserInitialDto userInitialDto, Object object) {
        return null;
    }
}
