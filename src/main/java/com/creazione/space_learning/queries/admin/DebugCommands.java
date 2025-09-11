package com.creazione.space_learning.queries.admin;

import com.creazione.space_learning.queries.CommandRegistry;
import com.creazione.space_learning.queries.GameCommand;
import com.creazione.space_learning.queries.Query;
import com.creazione.space_learning.utils.Answer;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.ArrayList;

@Component
@GameCommand(
        value = {"/debug-commands"},
        description = "Показать отладочную информацию о командах (только для разработчиков)"
)
public class DebugCommands extends Query {
    private final CommandRegistry commandRegistry;

    public DebugCommands(CommandRegistry commandRegistry) {
        super(new ArrayList<>());
        this.commandRegistry = commandRegistry;
    }

    @Override
    public Answer respond(Update update) {
        if (!isDeveloper(update.getMessage().getFrom().getId())) {
            Answer answer = new Answer();
            answer.setSendMessage(
                    sendCustomMessage(update.getMessage().getChatId(), "⛔ Доступ запрещен")
            );
            return answer;
        }

        StringBuilder sb = new StringBuilder("<b>⚙️ Зарегистрированные команды:</b>\n\n");
        commandRegistry.getCommandList().forEach(cmd -> {
            GameCommand ann = cmd.getClass().getAnnotation(GameCommand.class);
            sb.append("▸ ").append(String.join(", ", ann.value()))
                    .append("\n   → ").append(ann.description())
                    .append("\n   → Класс: ").append(cmd.getClass().getSimpleName())
                    .append("\n\n");
        });

        Answer answer = new Answer();
        answer.setSendMessage(
                sendCustomMessage(update.getMessage().getChatId(), sb.toString())
        );
        return answer;
    }

    private boolean isDeveloper(Long userId) {
        // Ваша логика проверки разработчиков
        return userId == 5773183764L; // Ваш ID в Telegram
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