package com.creazione.space_learning.queries.common;

import com.creazione.space_learning.dto.DataCentreAccessTextDto;
import com.creazione.space_learning.entities.game_entity.BuildingDto;
import com.creazione.space_learning.enums.BuildingType;
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
        value = {"/datacentre", "дата", "центр", "/datacentrenewwindow"},
        description = "Здание Дата Центра"
)
public class DataCentreAccess extends Query {
    private DataCentreAccessTextDto data;

    public DataCentreAccess() {
        super(List.of());
    }

    @Override
    public Answer respond(Update update) {
        data = new DataCentreAccessTextDto();
        Answer answer = new Answer();
        initialQuery(update, true);

        if (!isStatus()) {
            SendMessage sendMessage = getSendMessageFalse();
            answer.setSendMessage(sendMessage);
            return answer;
        }

        execute();

        if (update.hasCallbackQuery()) {
            answer.setAnswerCallbackQuery(closeRespond(update));
            if (update.getCallbackQuery().getData().equals("/datacentrenewwindow")) {
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
        StringBuilder text = new StringBuilder();
        text.append("<b>").append("Дата Центр ").append(getUserDto().getName()).append(":</b>\n\n");
        if (data.getBuilding() != null) {
            text.append("<b>Уровень</b>: ").append(data.getBuilding().getLevel()).append("\n");
        } else {
            text.append("<i>строение в разработке...</i>\n");//чтобы создать - /buildings\n");
        }
        text.append(getSpoiler());
        return text.toString();
    }

    @Override
    public InlineKeyboardMarkup getInlineKeyboardMarkup() {
        List<Integer> buttonsInLine = List.of(3, 2);
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        buttons.add(getButton(Emoji.ARROWS_COUNTERCLOCKWISE.toString(), "/datacentre"));
        buttons.add(getButton(Emoji.HOUSE.toString(), "/profile"));
        buttons.add((getButton(Emoji.EJECT_SYMBOL.toString(), "/datacentrenewwindow")));
        buttons.add((getButton("AI Action", "/english")));
        buttons.add((getButton("Торговля", "/trade")));
        return getKeyboard(buttonsInLine, buttons);
    }

    private void execute() {
        data.setBuilding(findDataCentreBuilding());

    }

    private BuildingDto findDataCentreBuilding() {
        List<BuildingDto> buildings = getUserDto().getBuildings();
        BuildingDto building = null;
        for (BuildingDto buildingDto : buildings) {
            if (buildingDto.getName().name().equals(BuildingType.DATA_CENTRE.name())) {
                building = buildingDto;
                break;
            }
        }
        return building;
    }
}
