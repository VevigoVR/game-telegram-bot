package com.creazione.space_learning.queries;

import com.creazione.space_learning.utils.Answer;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.ArrayList;

@Component
@GameCommand(value = {"__ai_fallback__"}, description = "Обработчик для будущего ИИ")
public class AIPlaceholder extends Query {
    private static final String AI_RESPONSE = """
        🤖 <b>Пока я только учусь!</b>
        
        Я будущий ИИ-помощник, который:
        - Будет помогать в игре
        - Объяснит игровые механики
        - Поможет с изучением английского
        - Просто поболтает с тобой
        
        <i>А пока используй игровые команды:</i>
        /help - список команд
        /study - изучение английского
        """;

    public AIPlaceholder() {
        super(new ArrayList<>());
    }

    @Override
    public Answer respond(Update update) {
        Long chatId = update.getMessage().getChatId();

        Answer answer = new Answer();
        answer.setSendMessage(sendCustomMessage(chatId, AI_RESPONSE));
        return answer;
    }

    @Override
    public SendPhoto getSendPhoto() {
        return null;
    }

    @Override
    public String getText() {
        return null;
    }

    @Override
    public InlineKeyboardMarkup getInlineKeyboardMarkup() {
        return null;
    }
}
