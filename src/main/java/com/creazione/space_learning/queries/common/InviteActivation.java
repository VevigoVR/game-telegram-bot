package com.creazione.space_learning.queries.common;

import com.creazione.space_learning.dto.CommandArgsDto;
import com.creazione.space_learning.dto.UserInitialDto;
import com.creazione.space_learning.queries.GameCommand;
import com.creazione.space_learning.queries.Query;
import com.creazione.space_learning.utils.Answer;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Component
@GameCommand(
        value = {"/invited_from"},
        description = "Активация кода приглашения"
)
public class InviteActivation extends Query<CommandArgsDto> {
    //private String[] args;

    public InviteActivation() {
        super(new ArrayList<>());
    }

    @Override
    public Answer respond(Update update) {
        Answer answer = new Answer();
        UserInitialDto userInitialDto = initialQuery(update, false);

        if (!userInitialDto.isStatus()) {
            SendMessage sendMessage = getSendMessageFalse(userInitialDto.getChatId());
            answer.setSendMessage(sendMessage);
            return answer;
        }

        // Извлекаем аргументы команды
        CommandArgsDto commandArgsDto = new CommandArgsDto(getCommandArgsAbsolute(userInitialDto, "/invited_from"));

        answer.setSendMessage(sendCustomMessage(userInitialDto.getChatId(), getText(userInitialDto, commandArgsDto)));
        return answer;
    }

    @Override
    public SendPhoto getSendPhoto(UserInitialDto userInitialDto, CommandArgsDto commandArgsDto) {
        String img = getImg();
        String text = getText(userInitialDto, commandArgsDto);
        SendPhoto message = sendCustomPhoto(userInitialDto.getChatId(), img, getTargetImg(), text);
        message.setReplyMarkup(getInlineKeyboardMarkup(userInitialDto, commandArgsDto));
        return message;
    }

    @Override
    public String getText(UserInitialDto userInitialDto, CommandArgsDto commandArgsDto) {
        String[] args = commandArgsDto.getArgs();
        if (args == null || args.length == 0) {
            return "❌ Пожалуйста, укажите реферальный код.\nПример: /invited_from ABCD1234";
        }

        String code = args[0];
        return processReferrerAndReferrals(code, getUserEntityFromDB(userInitialDto.getChatId()));
        /*
        try {
            referralService.activateReferralCode(userId, code);
            sendResponse(sender, update, "✅ Реферальный код активирован! Вы получили:\n"
                    + "- 200 золота\n"
                    + "- Стартовый набор ресурсов");
        } catch (InvalidReferralCodeException e) {
            sendResponse(sender, update, "❌ Неверный реферальный код. Проверьте правильность ввода.");
        } catch (SelfReferralException e) {
            sendResponse(sender, update, "❌ Нельзя использовать собственный реферальный код");
        } catch (AlreadyActivatedException e) {
            sendResponse(sender, update, "ℹ️ Вы уже активировали реферальный код ранее");
        } catch (Exception e) {
            log.error("Error processing referral code", e);
            sendResponse(sender, update, "⚠️ Произошла ошибка при обработке кода. Попробуйте позже.");
        }

         */
    }

    @Override
    public InlineKeyboardMarkup getInlineKeyboardMarkup(UserInitialDto userInitialDto, CommandArgsDto commandArgsDto) {
        List<Integer> buttonsInLine = List.of(0);
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        //buttons.add(getButton(Emoji.ARROWS_COUNTERCLOCKWISE.toString(), "/profile"));
        //buttons.add(getButton("Строения", "/buildings"));
        //buttons.add(getButton("Склад", "/resources"));
        //buttons.add(getButton("Фондовый рынок", "/market"));
        //buttons.add(getButton("Поиск ресурсов", "/expedition"));
        return getKeyboard(buttonsInLine, buttons);
    }
}
