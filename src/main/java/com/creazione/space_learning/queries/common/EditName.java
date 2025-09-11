package com.creazione.space_learning.queries.common;

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
public class EditName extends Query {
    private String[] args;
    private String[] args2;
    private String name = "";
    private String wrong = "";

    public EditName() {
        super(List.of());
    }

    @Override
    public Answer respond(Update update) {
        name = ""; // чтобы не сохранялся старый результат
        Answer answer = new Answer();
        initialQuery(update, false);

        if (!isStatus()) {
            SendMessage sendMessage = getSendMessageFalse();
            answer.setSendMessage(sendMessage);
            return answer;
        }

        // Извлекаем аргументы команды
        args = getCommandArgsAbsolute("/edit");
        args2 = getCommandArgsAbsolute("изменить");

        if (args != null && args[0].equals("name")) {
            //System.out.println("args != null && args[0].equals(\"name\")");
            name = takeName(args);
        } else if (args2 != null && (args2[0].equals("ник") || args2[0].equals("имя"))) {
            //System.out.println("args2 != null && (args2[0].equals(\"ник\") || args2[0].equals(\"имя\"))");
            name = takeName(args2);
        }

        if (!name.isEmpty()) {
            int i = userService.updateNameById(getUserDto().getId(), name);
            if (i < 1) {
                wrong = "❌ Обновить имя не удалось. \nПопробуйте ещё раз немного позже.";
            } else if (i > 2) {
                wrong = "❌ Обновить имя удалось. \nНо не только Вам...";
            }
        }
        SendMessage sendMessage = takeSendMessage();
        answer.setSendMessage(sendMessage);
        return answer;
    }

    private SendMessage takeSendMessage() {
        return sendCustomMessage(getChatId(), getText());
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
        if (!wrong.isEmpty()) {
            return wrong;
        } else if (name.isEmpty()) {
            return  "⚠️ Смена имени не удалась! \nЧтобы изменить имя: \n/edit name Имя";
        } else {
            return "✅ Смена имени завершена успешно! \nТеперь Вы: " + name;
        }
    }

    @Override
    public InlineKeyboardMarkup getInlineKeyboardMarkup() {
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