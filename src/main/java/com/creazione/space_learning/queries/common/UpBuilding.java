package com.creazione.space_learning.queries.common;

import com.creazione.space_learning.entities.postgres.BuildingP;
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
        value = {"/upgold", "/upwood", "/upstone", "/upmetal"},
        description = "Улучшение постройки"
)
public class UpBuilding extends Query {
    private BuildingInfo buildingInfo;
    private int iBuilding;

    public UpBuilding() {
        super(List.of("/upgold", "/upwood", "/upstone", "/upmetal"));
    }

    @Override
    public Answer respond(Update update) {
        buildingInfo = new BuildingInfo();

        Answer answer = new Answer();
        initialQuery(update, true);

        if (!isStatus()) {
            SendMessage sendMessage = getSendMessageFalse();
            answer.setSendMessage(sendMessage);
            return answer;
        }

        buildingInfo.setQuery(getQuery().replace("/up", "/building"));
        if (update.hasCallbackQuery()) {
            buildingInfo.setUserDto(getUserDto());
            answer.setAnswerCallbackQuery(closeRespond(update));
            setBuildingObjects(getQuery());
            EditMessageCaption newText = EditMessageCaption.builder()
                    .chatId(getChatId())
                    .messageId(getMessageId())
                    .build();
            newText.setReplyMarkup(getInlineKeyboardMarkup());
            newText.setCaption(getText());
            newText.setParseMode(ParseMode.HTML);
            answer.setEditMessageCaption(newText);
        } else {
            buildingInfo.setUserDto(getUserDto());
            setBuildingObjects(getQuery());
            answer.setSendPhoto(getSendPhoto());
        }
        return answer;
    }

    @Override
    public InlineKeyboardMarkup getInlineKeyboardMarkup() {
        return buildingInfo.getInlineKeyboardMarkup(true);
    }

    @Override
    public String getText() {
        return upLevel() + buildingInfo.getText();
    }

    @Override
    public SendPhoto getSendPhoto() {
        String img = getImg();
        String text = getText();
        SendPhoto message = sendCustomPhoto(getChatId(), img, getTargetImg(), text);
        message.setReplyMarkup(getInlineKeyboardMarkup());
        return message;
    }

    private void setBuildingObjects(String query) {
        switch (query) {
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
            case "/upmetal" : {
                buildingInfo.setTargetBuilding(new MetalBuilding());
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
            case "/upstone" : {
                buildingInfo.setTargetBuilding(new StoneBuilding());
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
            case "/upwood" : {
                buildingInfo.setTargetBuilding(new WoodBuilding());
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
        }
    }

    private String upLevel() {
        if (buildingInfo.isHasBuilding()) {
            return buildingService.upLevel(getUserDto(), iBuilding);
        } else {
            return buildingService.createBuilding(getUserDto(), buildingInfo.getTargetBuilding());
        }
    }
}
