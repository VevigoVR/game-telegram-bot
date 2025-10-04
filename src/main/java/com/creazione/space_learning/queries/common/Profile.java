package com.creazione.space_learning.queries.common;

import com.creazione.space_learning.dto.UserInitialDto;
import com.creazione.space_learning.entities.game_entity.BuildingDto;
import com.creazione.space_learning.entities.game_entity.UserDto;
import com.creazione.space_learning.queries.GameCommand;
import com.creazione.space_learning.queries.Query;
import com.creazione.space_learning.utils.Answer;
import com.creazione.space_learning.enums.Emoji;
import com.creazione.space_learning.utils.Formatting;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
@GameCommand(
        value = {"/profile", "профиль", "аккаунт", ".profile", "/profilenewwindow"},
        description = "Просмотр профиля игрока"
)
public class Profile extends Query {
    public Profile() {
        super(List.of("/profile", "/profilenewwindow"));
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
            if (update.getCallbackQuery().getData().equals("/profilenewwindow")) {
                answer.setSendPhoto(getSendPhoto(userInitialDto, null));
                return answer;
            }
            EditMessageCaption newText = EditMessageCaption.builder()
                    .chatId(userInitialDto.getChatId())
                    .messageId(userInitialDto.getMessageId())
                    .build();
            newText.setReplyMarkup(getInlineKeyboardMarkup(userInitialDto, null));
            newText.setCaption(getText(userInitialDto, null));
            newText.setParseMode(ParseMode.HTML);
            answer.setEditMessageCaption(newText);
        } else {
            answer.setSendPhoto(getSendPhoto(userInitialDto, null));
        }
        return answer;
    }

    public Answer respondWithoutUser(Update update, UserInitialDto userInitialDto) {
        UserDto userDto = userInitialDto.getUserDto();
        boolean isUpdateDB = resourceService.calculateQuantityChanges(userDto, Instant.now());
        if (isUpdateDB) {
            userService.saveFull(userDto);
        }
        userInitialDto.setChatId(update.getMessage().getChatId());
        userInitialDto.setUserDto(userDto);
        Answer answer = new Answer();
        answer.setSendPhoto(getSendPhoto(userInitialDto, null));
        return answer;
    }

    @Override
    public SendPhoto getSendPhoto(UserInitialDto userInitialDto, Object noObject) {
        String img = getImg();
        String text = getText(userInitialDto, null);
        SendPhoto message = sendCustomPhoto(userInitialDto.getChatId(), img, getTargetImg(), text);
        message.setReplyMarkup(getInlineKeyboardMarkup(userInitialDto, null));
        return message;
    }

    @Override
    public String getText(UserInitialDto userInitialDto, Object noObject) {
        UserDto userDto = userInitialDto.getUserDto();
        StringBuilder text = new StringBuilder();
        long pointsLong = userDto.getPlayerScore().getScore();
        String points = Formatting.formatWithDots(pointsLong);
        System.out.println("points: " + points);
        text.append("<b>").append("Планета ").append(userDto.getName()).append(":</b>\n\n");
        if (pointsLong > 0) {
            text.append("<b>Набрано очков</b>:\n").append(points).append("\n\n");
        }
        text.append("<b>Строения</b>:\n");

        if (!userDto.viewSortedBuildings().isEmpty()) {
            for (BuildingDto building : userDto.viewSortedBuildings()) {
                text.append(Emoji.WHITE_SMALL_SQUARE).append(" ").append(building.getName()).append(": ")
                        .append(building.getLevel())
                        .append(" уровень\n");
            }
        } else {
            text.append("<i>строений нет...</i>\n");//чтобы создать - /buildings\n");
        }

        text.append(getSpoiler());

        return text.toString();
    }

    @Override
    public InlineKeyboardMarkup getInlineKeyboardMarkup(UserInitialDto userInitialDto, Object noObject) {
        List<Integer> buttonsInLine = List.of(2, 3, 2);
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        buttons.add(getButton(Emoji.ARROWS_COUNTERCLOCKWISE.toString(), "/profile"));
        buttons.add((getButton(Emoji.EJECT_SYMBOL.toString(), "/profilenewwindow")));

        buttons.add(getButton("Шахты", "/buildings"));
        buttons.add(getButton(Emoji.SATELLITE.toString(), "/datacentre"));
        buttons.add(getButton("Склад", "/resources"));

        buttons.add(getButton(Emoji.BUSTS_IN_SILHOUETTE.toString(), "/referrals"));
        //buttons.add(getButton(Emoji.GEAR.toString(), "/help"));
        if (userInitialDto.getUserDto().isPost()) {
            buttons.add(getButton(Emoji.POST_WITH_MAILS.toString(), "/post"));
        } else {
            buttons.add(getButton(Emoji.POST_WITHOUT_MAILS.toString(), "/post"));
        }

        //buttons.add(getButton("Фондовый рынок", "/market"));
        //buttons.add(getButton("Поиск ресурсов", "/expedition"));
        return getKeyboard(buttonsInLine, buttons);
    }
}