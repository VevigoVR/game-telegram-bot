package com.creazione.space_learning.queries.common;

import com.creazione.space_learning.dto.DataCentreAccessTextDto;
import com.creazione.space_learning.dto.UserInitialDto;
import com.creazione.space_learning.entities.game_entity.BuildingDto;
import com.creazione.space_learning.entities.game_entity.UserDto;
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
public class DataCentreAccess extends Query<DataCentreAccessTextDto> {
    //private DataCentreAccessTextDto data;

    public DataCentreAccess() {
        super(List.of());
    }

    @Override
    public Answer respond(Update update) {
        DataCentreAccessTextDto data = new DataCentreAccessTextDto();
        Answer answer = new Answer();
        UserInitialDto userInitialDto = initialQuery(update, true);

        if (!userInitialDto.isStatus()) {
            SendMessage sendMessage = getSendMessageFalse(userInitialDto.getChatId());
            answer.setSendMessage(sendMessage);
            return answer;
        }

        execute(userInitialDto.getUserDto(), data);

        if (update.hasCallbackQuery()) {
            answer.setAnswerCallbackQuery(closeRespond(update));
            if (update.getCallbackQuery().getData().equals("/datacentrenewwindow")) {
                answer.setSendPhoto(getSendPhoto(userInitialDto, data));
                return answer;
            }
            EditMessageCaption newText = EditMessageCaption.builder()
                    .chatId(userInitialDto.getChatId())
                    .messageId(userInitialDto.getMessageId())
                    .build();
            newText.setReplyMarkup(getInlineKeyboardMarkup(userInitialDto, null));
            newText.setCaption(getText(userInitialDto, data));
            newText.setParseMode(ParseMode.HTML);
            answer.setEditMessageCaption(newText);
        } else {
            answer.setSendPhoto(getSendPhoto(userInitialDto, data));
        }
        return answer;
    }

    @Override
    public SendPhoto getSendPhoto(UserInitialDto userInitialDto, DataCentreAccessTextDto data) {
        String img = getImg();
        String text = getText(userInitialDto, data);
        SendPhoto message = sendCustomPhoto(userInitialDto.getChatId(), img, getTargetImg(), text);
        message.setReplyMarkup(getInlineKeyboardMarkup(userInitialDto, null));
        return message;
    }

    @Override
    public String getText(UserInitialDto userInitialDto, DataCentreAccessTextDto data) {
        StringBuilder text = new StringBuilder();
        text.append("<b>").append("Дата Центр ").append(userInitialDto.getUserDto().getName()).append(":</b>\n\n");
        if (data.getBuilding() != null) {
            text.append("<b>Уровень</b>: ").append(data.getBuilding().getLevel()).append("\n");
        } else {
            text.append("<i>строение в разработке...</i>\n");//чтобы создать - /buildings\n");
        }
        text.append(getSpoiler());
        return text.toString();
    }

    @Override
    public InlineKeyboardMarkup getInlineKeyboardMarkup(UserInitialDto userInitialDto, DataCentreAccessTextDto noObject) {
        List<Integer> buttonsInLine = List.of(3, 2);
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        buttons.add(getButton(Emoji.ARROWS_COUNTERCLOCKWISE.toString(), "/datacentre"));
        buttons.add(getButton(Emoji.HOUSE.toString(), "/profile"));
        buttons.add((getButton(Emoji.EJECT_SYMBOL.toString(), "/datacentrenewwindow")));
        buttons.add((getButton("AI Action", "/english")));
        buttons.add((getButton("Торговля", "/trade")));
        return getKeyboard(buttonsInLine, buttons);
    }

    private void execute(UserDto userDto, DataCentreAccessTextDto data) {
        data.setBuilding(findDataCentreBuilding(userDto));

    }

    private BuildingDto findDataCentreBuilding(UserDto userDto) {
        List<BuildingDto> buildings = userDto.getBuildings();
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
