package com.creazione.space_learning.queries.common;

import com.creazione.space_learning.dto.UserInitialDto;
import com.creazione.space_learning.entities.game_entity.BuildingDto;
import com.creazione.space_learning.entities.game_entity.ResourceDto;
import com.creazione.space_learning.entities.game_entity.UserDto;
import com.creazione.space_learning.game.buildings.BuildingList;
import com.creazione.space_learning.enums.BuildingType;
import com.creazione.space_learning.queries.GameCommand;
import com.creazione.space_learning.queries.Query;
import com.creazione.space_learning.utils.Answer;
import com.creazione.space_learning.enums.Emoji;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Component
@GameCommand(
        value = {"/buildings", "строения", "здания"},
        description = "Список всех построек"
)
public class BuildingsQuery extends Query {
    public BuildingsQuery() {
        super(List.of());
    }

    @Override
    public Answer respond(Update update) {
        return getCommonRespond(update, true);
    }

    @Override
    public SendPhoto getSendPhoto(UserInitialDto userInitialDto, Object noObject) {
        String img = getImg();
        String text = getText(userInitialDto, null);
        String targetImg = getTargetImg();
        SendPhoto message = sendCustomPhoto(userInitialDto.getChatId(), img, targetImg, text);
        message.setReplyMarkup(getInlineKeyboardMarkup(null, null));
        return message;
    }

    @Override
    public String getText(UserInitialDto userInitialDto, Object noObject) {
        UserDto userDto = userInitialDto.getUserDto();
        StringBuilder text = new StringBuilder();
        text.append("<b>").append("Производство ").append(userDto.getName()).append("</b>\n\n");
        List<BuildingDto> buildingList = userDto.viewSortedBuildings().stream().filter(BuildingDto::isVisible).toList();
        if (buildingList.isEmpty()) {
            text.append("<i>строений производства нет...</i>\n");
        }
        for (BuildingDto building : buildingList) {
            text.append(Emoji.WHITE_SMALL_SQUARE)
                    .append(" ")
                    .append(building.getName().toString().toUpperCase())
                    .append(" - ")
                    .append(building.getLevel())
                    .append(" уровень\n");
        }

        if (buildingList.size() < 2) {
            text.append("\n<b>Можно построить</b>:\n");
        }

        for (BuildingDto building : BuildingList.BUILDING_LIST) {
            if (!building.isVisible()) {
                continue;
            }
            boolean isExist = false;
            for (BuildingDto myBuilding : userDto.getBuildings()) {
                if (building.getName().equals(myBuilding.getName())) {
                    isExist = true;
                }
            }
            if (isExist) continue;

            text.append(Emoji.WHITE_SMALL_SQUARE).append(" ").append(building.getName().toString().toUpperCase()).append("\n");
            text.append("Стоимость строительства:").append("\n");
            List<ResourceDto> resources = building.viewPrice(1);
            for (ResourceDto resource : resources) {
                text.append(resource.getName()).append(": ").append(resource.makeQuantityString())
                        .append(" ").append(resource.getEmoji()).append("\n");
            }
            text.append("Время строительства:");
            long duration = buildingService.getDuration(resources);
            text.append(buildingService.getDurationToString(duration)).append("\n\n");
        }
        return text.toString();
    }

    @Override
    public InlineKeyboardMarkup getInlineKeyboardMarkup(UserInitialDto userInitialDto, Object noObject) {
        List<Integer> buttonsInLine = List.of(1, 2);
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        buttons.add(getButton(Emoji.HOUSE.toString(), "/profile"));
        // УДАЛЯЕМ ЗОЛОТУЮ ШАХТУ И ЛЕСОПИЛКУ
        //buttons.add(getButton(BuildingType.GOLD_BUILDING.toString(), "/buildingGold"));
        buttons.add(getButton(BuildingType.STONE_BUILDING.toString(), "/buildingStone"));
        buttons.add(getButton(BuildingType.METAL_BUILDING.toString(), "/buildingMetal"));
        //buttons.add(getButton(BuildingType.WOOD_BUILDING.toString(), "/buildingWood"));
        return getKeyboard(buttonsInLine, buttons);
    }
}
