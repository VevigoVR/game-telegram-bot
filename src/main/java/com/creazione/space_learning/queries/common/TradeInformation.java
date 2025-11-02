package com.creazione.space_learning.queries.common;

import com.creazione.space_learning.dto.UserInitialDto;
import com.creazione.space_learning.entities.game_entity.ResourceDto;
import com.creazione.space_learning.game.resources.Gold;
import com.creazione.space_learning.game.resources.ResourceList;
import com.creazione.space_learning.queries.GameCommand;
import com.creazione.space_learning.queries.Query;
import com.creazione.space_learning.utils.Answer;
import com.creazione.space_learning.enums.Emoji;
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
        value = {"/trade", "торговля", "/tradenewwindow"},
        description = "Информация о торговле с NPC"
)
public class TradeInformation extends Query {
    //List<ResourceDto> resourcesForTrade;

    public TradeInformation() {
        super(List.of());
    }

    @Override
    public Answer respond(Update update) {
        Answer answer = new Answer();
        UserInitialDto userInitialDto = initialQuery(update, true);

        if (!userInitialDto.isStatus()) {
            SendMessage sendMessage = getSendMessageFalse(userInitialDto.getChatId());
            answer.setSendMessage(sendMessage);
            return answer;
        }

        if (update.hasCallbackQuery()) {
            answer.setAnswerCallbackQuery(closeRespond(update));
            if (update.getCallbackQuery().getData().equals("/tradenewwindow")) {
                answer.setSendPhoto(getSendPhoto(userInitialDto, null));
                return answer;
            }
            EditMessageCaption newText = EditMessageCaption.builder()
                    .chatId(userInitialDto.getChatId())
                    .messageId(userInitialDto.getMessageId())
                    .build();
            newText.setReplyMarkup(getInlineKeyboardMarkup(null, null));
            newText.setCaption(getText(null, null));
            newText.setParseMode(ParseMode.HTML);
            answer.setEditMessageCaption(newText);
        } else {
            answer.setSendPhoto(getSendPhoto(userInitialDto, null));
        }
        return answer;
    }

    @Override
    public SendPhoto getSendPhoto(UserInitialDto userInitialDto, Object noObject) {
        String img = getImg();
        String text = getText(null, null);
        SendPhoto message = sendCustomPhoto(userInitialDto.getChatId(), img, getTargetImg(), text);
        message.setReplyMarkup(getInlineKeyboardMarkup(null, null));
        return message;
    }

    @Override
    public String getText(UserInitialDto userInitialDto, Object noObject) {
        StringBuilder text = new StringBuilder();
        text.append("<b>").append("Дата Центр. Торговля ресурсами:</b>\n\n")
        .append(getActualInfo()).append("\n")
        .append("""
            Пример сделки:
            <code>/sell metal 100</code>
            <code>/buy metal 5k</code>
            <code>/buy stone 5kk</code>
            <code>/sell stone 10mk</code>
            (нажмите, чтобы скопировать)
            """);
        text.append(getSpoiler());
        return text.toString();
    }

    @Override
    public InlineKeyboardMarkup getInlineKeyboardMarkup(UserInitialDto userInitialDto, Object noObject) {
        List<Integer> buttonsInLine = List.of(2);
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        buttons.add(getButton(Emoji.HOUSE.toString(), "/profile"));
        buttons.add((getButton(Emoji.EJECT_SYMBOL.toString(), "/tradenewwindow")));

        return getKeyboard(buttonsInLine, buttons);
    }

    private String getActualInfo() {
        Gold gold = new Gold();
        StringBuilder text = new StringBuilder();
        if (ResourceList.TRADE_RESOURCES_LIST.isEmpty()) {
            return "";
        }
        for (ResourceDto resource : ResourceList.TRADE_RESOURCES_LIST) {
            text.append(resource.getEmoji()).append(" <b>").append(resource.getName().getName())
                    .append("</b>\nПродажа: за каждые 100 шт. прибавится ")
                    .append(gold.getEmoji()).append(" ").append((int) (100 * resource.getSellForGold())).append(" шт.\n")
                    .append("Покупка: за каждые 100 шт. потребуется ")
                    .append(gold.getEmoji()).append(" ").append((int) (100 * resource.getBuyForGold())).append(" шт.\n\n");
        }
        return text.toString();
    }
}