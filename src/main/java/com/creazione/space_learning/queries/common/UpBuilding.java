package com.creazione.space_learning.queries.common;

import com.creazione.space_learning.dto.UpdateBuildingDto;
import com.creazione.space_learning.dto.UserInitialDto;
import com.creazione.space_learning.entities.game_entity.BuildingDto;
import com.creazione.space_learning.entities.game_entity.UserDto;
import com.creazione.space_learning.game.buildings.*;
import com.creazione.space_learning.queries.GameCommand;
import com.creazione.space_learning.queries.Query;
import com.creazione.space_learning.utils.Answer;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;

@Component
@GameCommand(
        // УДАЛЯЕМ ЗОЛОТУЮ ШАХТУ value = {"/upgold",
        value = {"/upwood", "/upstone", "/upmetal"},
        description = "Улучшение постройки"
)
public class UpBuilding extends Query<UpdateBuildingDto> {
    //private BuildingInfo buildingInfo;
    //private int iBuilding;

    // УДАЛЯЕМ ЗОЛОТУЮ ШАХТУ public UpBuilding() { super(List.of("/upgold",
    public UpBuilding() {
        super(List.of("/upwood", "/upstone", "/upmetal"));
    }

    @Override
    public Answer respond(Update update) {
        UpdateBuildingDto updateBuildingDto = new UpdateBuildingDto();
        Answer answer = new Answer();
        UserInitialDto userInitialDto = initialQuery(update, true);

        if (!userInitialDto.isStatus()) {
            SendMessage sendMessage = getSendMessageFalse(userInitialDto.getChatId());
            answer.setSendMessage(sendMessage);
            return answer;
        }

        if (update.hasCallbackQuery()) {
            answer.setAnswerCallbackQuery(closeRespond(update));
            setBuildingObjects(userInitialDto, updateBuildingDto);
            EditMessageCaption newText = EditMessageCaption.builder()
                    .chatId(userInitialDto.getChatId())
                    .messageId(userInitialDto.getMessageId())
                    .build();
            newText.setReplyMarkup(getInlineKeyboardMarkup(userInitialDto, updateBuildingDto));
            newText.setCaption(getText(userInitialDto, updateBuildingDto));
            newText.setParseMode(ParseMode.HTML);
            answer.setEditMessageCaption(newText);
        } else {
            setBuildingObjects(userInitialDto, updateBuildingDto);
            answer.setSendPhoto(getSendPhoto(userInitialDto, updateBuildingDto));
        }
        return answer;
    }

    @Override
    public InlineKeyboardMarkup getInlineKeyboardMarkup(UserInitialDto userInitialDto, UpdateBuildingDto updateBuildingDto) {
        BuildingInfo buildingInfo = new BuildingInfo();
        UserInitialDto transferInitialDto = new UserInitialDto(
                userInitialDto.getUserDto(),
                userInitialDto.getQuery().replace("/up", "/building"),
                userInitialDto.getMessageId(),
                userInitialDto.getChatId(),
                userInitialDto.isStatus(),
                userInitialDto.isUpdate()
        );
        return buildingInfo.getInlineKeyboardMarkup(transferInitialDto, updateBuildingDto);
    }

    @Override
    public String getText(UserInitialDto userInitialDto, UpdateBuildingDto updateBuildingDto) {
        BuildingInfo buildingInfo = new BuildingInfo();
        UserInitialDto transferInitialDto = new UserInitialDto(
                userInitialDto.getUserDto(),
                userInitialDto.getQuery().replace("/up", "/building"),
                userInitialDto.getMessageId(),
                userInitialDto.getChatId(),
                userInitialDto.isStatus(),
                userInitialDto.isUpdate()
        );
        return upLevel(userInitialDto, updateBuildingDto) + buildingInfo.getText(transferInitialDto, updateBuildingDto);
    }

    @Override
    public SendPhoto getSendPhoto(UserInitialDto userInitialDto, UpdateBuildingDto updateBuildingDto) {
        String img = getImg();
        String text = getText(userInitialDto, updateBuildingDto);
        SendPhoto message = sendCustomPhoto(userInitialDto.getChatId(), img, getTargetImg(), text);
        message.setReplyMarkup(getInlineKeyboardMarkup(userInitialDto, updateBuildingDto));
        return message;
    }

    private void setBuildingObjects(UserInitialDto userInitialDto, UpdateBuildingDto updateBuildingDto) {
        UserDto userDto = userInitialDto.getUserDto();
        switch (userInitialDto.getQuery()) {
            // УДАЛЯЕМ ЗОЛОТУЮ ШАХТУ
            /*
            case "/upgold" : {
                buildingInfo.setTargetBuilding(new GoldBuilding());
                for (int i = 0; i < getUserDto().getBuildings().size(); i++) {
                    BuildingP building = getUserDto().getBuildings().get(i);
                    if (building.getName().equals(buildingInfo.getTargetBuilding().getName())) {
                        buildingInfo.setUserBuilding(building);
                        buildingInfo.setHasBuilding(true);
                        iBuilding = i;
                        break;
                    }
                }
                break;
            }
             */
            case "/upmetal" : {
                updateBuildingDto.setTargetBuilding(new MetalBuilding());
                for (int i = 0; i < userDto.getBuildings().size(); i++) {
                    BuildingDto building = userDto.getBuildings().get(i);
                    if (building.getName().equals(updateBuildingDto.getTargetBuilding().getName())) {
                        updateBuildingDto.setUserBuilding(building);
                        updateBuildingDto.setHasBuilding(true);
                        updateBuildingDto.setIBuilding(i);
                        break;
                    }
                }
                break;
            }
            case "/upstone" : {
                updateBuildingDto.setTargetBuilding(new StoneBuilding());
                for (int i = 0; i < userDto.getBuildings().size(); i++) {
                    BuildingDto building = userDto.getBuildings().get(i);
                    if (building.getName().equals(updateBuildingDto.getTargetBuilding().getName())) {
                        updateBuildingDto.setUserBuilding(building);
                        updateBuildingDto.setHasBuilding(true);
                        updateBuildingDto.setIBuilding(i);
                        break;
                    }
                }
                break;
            }
            case "/upwood" : {
                updateBuildingDto.setTargetBuilding(new WoodBuilding());
                for (int i = 0; i < userDto.getBuildings().size(); i++) {
                    BuildingDto building = userDto.getBuildings().get(i);
                    if (building.getName().equals(updateBuildingDto.getTargetBuilding().getName())) {
                        updateBuildingDto.setUserBuilding(building);
                        updateBuildingDto.setHasBuilding(true);
                        updateBuildingDto.setIBuilding(i);
                        break;
                    }
                }
                break;
            }
        }
    }

    private String upLevel(UserInitialDto userInitialDto, UpdateBuildingDto updateBuildingDto) {
        if (updateBuildingDto.isHasBuilding()) {
            return buildingService.upLevel(userInitialDto.getUserDto(), updateBuildingDto.getIBuilding());
        } else {
            return buildingService.createBuilding(userInitialDto.getUserDto(), updateBuildingDto.getTargetBuilding());
        }
    }
}
