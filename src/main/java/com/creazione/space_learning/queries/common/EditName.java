package com.creazione.space_learning.queries.common;

import com.creazione.space_learning.dto.UserInitialDto;
import com.creazione.space_learning.dto.WrongMessage;
import com.creazione.space_learning.entities.game_entity.UserDto;
import com.creazione.space_learning.queries.GameCommand;
import com.creazione.space_learning.queries.Query;
import com.creazione.space_learning.utils.Answer;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;

@Component
@GameCommand(
        value = {"/edit", "изменить"},
        description = "Изменить имя"
)
public class EditName extends Query<WrongMessage> {
    //private String[] args;
    //private String[] args2;
    //private String name = "";
    //private String wrong = "";

    public EditName() {
        super(List.of());
    }

    @Override
    public Answer respond(Update update) {
        String name = "";
        Answer answer = new Answer();
        UserInitialDto userInitialDto = initialQuery(update, false);

        if (!userInitialDto.isStatus()) {
            SendMessage sendMessage = getSendMessageFalse(userInitialDto.getChatId());
            answer.setSendMessage(sendMessage);
            return answer;
        }
        WrongMessage wrongMessage = new WrongMessage();
        // Извлекаем аргументы команды
        String[] args = getCommandArgsAbsolute(userInitialDto, "/edit");
        String[] args2 = getCommandArgsAbsolute(userInitialDto, "изменить");

        if (args != null && args[0].equals("name")) {
            //System.out.println("args != null && args[0].equals(\"name\")");
            name = takeName(args);
        } else if (args2 != null && (args2[0].equals("ник") || args2[0].equals("имя"))) {
            //System.out.println("args2 != null && (args2[0].equals(\"ник\") || args2[0].equals(\"имя\"))");
            name = takeName(args2);
        }

        if (!name.isEmpty()) {
            int i = userService.updateNameById(userInitialDto.getUserDto().getId(), name, userInitialDto.getUserDto().getTelegramId());
            if (i < 1) {
                wrongMessage.setText("❌ Обновить имя не удалось. \nПопробуйте ещё раз немного позже.");
            } else if (i > 2) {
                wrongMessage.setText("❌ Обновить имя удалось. \nНо не только Вам...");
            }
        } else {
            wrongMessage.setText("⚠️ Смена имени не удалась! \nЧтобы изменить имя: \n/edit name Имя");
        }
        userInitialDto.getUserDto().setName(name);
        SendMessage sendMessage = takeSendMessage(userInitialDto, wrongMessage);
        answer.setSendMessage(sendMessage);
        return answer;
    }

    private SendMessage takeSendMessage(UserInitialDto userInitialDto, WrongMessage wrongMessage) {
        return sendCustomMessage(userInitialDto.getChatId(), getText(userInitialDto, wrongMessage));
    }


    @Override
    public String getText(UserInitialDto userInitialDto, WrongMessage wrongMessage) {
        if (!wrongMessage.getText().isEmpty()) {
            return wrongMessage.getText();
        } else {
            return "✅ Смена имени завершена успешно! \nТеперь Вы: " + userInitialDto.getUserDto().getName();
        }
    }

    @Override
    public SendPhoto getSendPhoto(UserInitialDto userInitialDto, WrongMessage wrongMessage) {
        return null;
    }

    @Override
    public InlineKeyboardMarkup getInlineKeyboardMarkup(UserInitialDto userInitialDto, WrongMessage wrongMessage) {
        return null;
    }

    private String takeName(String[] args) {
        String name = "";
        if (args.length < 2 || args.length > 5) { return ""; }
        for (int i = 1; i < args.length; i++) {
            name += args[i].substring(0, 1).toUpperCase() + args[i].substring(1) + " ";
        }
        return HtmlUtils.htmlEscape(name).trim();
    }
}