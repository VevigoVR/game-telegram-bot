package com.creazione.space_learning.queries.common;

import com.creazione.space_learning.dto.ReferralStats;
import com.creazione.space_learning.queries.GameCommand;
import com.creazione.space_learning.queries.Query;
import com.creazione.space_learning.utils.Answer;
import com.creazione.space_learning.enums.Emoji;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Component
@GameCommand(
        value = {"/referrals", "/referralsnw"},
        description = "Статистика приглашённых игроков"
)
public class ReferralsStatistic extends Query {
    @Value("${bot.name}")
    String botName;

    public ReferralsStatistic() {
        super(new ArrayList<>());
    }

    @Override
    public Answer respond(Update update) {
        Answer answer = new Answer();
        initialQuery(update, false);

        if (!isStatus()) {
            SendMessage sendMessage = getSendMessageFalse();
            answer.setSendMessage(sendMessage);
            return answer;
        }

        if (update.hasCallbackQuery()) {
            answer.setAnswerCallbackQuery(closeRespond(update));
            if (update.getCallbackQuery().getData().equals("/referralsnw")) {
                answer.setSendPhoto(getSendPhoto());
                return answer;
            }
            EditMessageCaption newText = EditMessageCaption.builder()
                    .chatId(getChatId())
                    .messageId(getMessageId())
                    .build();
            newText.setReplyMarkup(getInlineKeyboardMarkup());
            newText.setCaption(getText());
            newText.setParseMode(ParseMode.HTML);
            answer.setEditMessageCaption(newText);
        } else {
            answer.setSendPhoto(getSendPhoto());
        }
        return answer;
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
        Long id = getUserDto().getId();
        ReferralStats stats = referralService.getStats(getUserDto());

        StringBuilder message = new StringBuilder(String.format(
                "📊 <b>Ваша реферальная статистика:</b>\n" +
                        "• Всего приглашено: %d",
                stats.getTotalReferrals()
        ));

        message.append("\n\n <b>Реферальная ссылка:</b> ");

        message.append("<code>https://t.me/")
                .append(botName)
                .append("?start=ref_")
                .append(referralService.getReferralCode(id))
                .append("</code>");

        message.append("""
                
                
                Если Ваш человек случайно перешёл сюда без реферальной ссылки, \
                попросите его ввести команду:
                
                <code>/invited_from\s""")
                .append(referralService.getReferralCode(id))
                .append("</code>");
        //message.append("\n\nВаш ID: ").append(referralService.decodeReferralCode(referralService.getReferralCode(id)));
        message.append(getSpoiler());
        return message.toString();
    }

    @Override
    public InlineKeyboardMarkup getInlineKeyboardMarkup() {
        List<Integer> buttonsInLine = List.of(3);
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        buttons.add(getButton(Emoji.ARROWS_COUNTERCLOCKWISE.toString(), "/referrals"));
        buttons.add(getButton(Emoji.HOUSE.toString(), "/profile"));
        buttons.add((getButton(Emoji.EJECT_SYMBOL.toString(), "/referralsnw")));
        return getKeyboard(buttonsInLine, buttons);
    }
}
