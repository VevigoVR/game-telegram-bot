package com.creazione.space_learning.queries.common;

import com.creazione.space_learning.config.DataSet;
import com.creazione.space_learning.entities.postgres.ActiveBoosterP;
import com.creazione.space_learning.entities.postgres.BuildingP;
import com.creazione.space_learning.game.buildings.*;
import com.creazione.space_learning.entities.postgres.ResourceP;
import com.creazione.space_learning.enums.ResourceType;
import com.creazione.space_learning.queries.GameCommand;
import com.creazione.space_learning.queries.Query;
import com.creazione.space_learning.utils.Answer;
import com.creazione.space_learning.enums.Emoji;
import com.creazione.space_learning.utils.Formatting;
import lombok.Getter;
import lombok.Setter;
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

@Setter
@Getter
@Component
@GameCommand(
        // УДАЛЯЕМ ЗОЛОТУЮ ШАХТУ value = {"/buildinggold",
        value = {
                "/buildingmetal",
                "/buildingstone",
                "/buildingwood"},
        description = "Информация о здании"
)
public class BuildingInfo extends Query {
    private BuildingP targetBuilding;
    private BuildingP userBuilding;
    private boolean hasBuilding = false;

    public BuildingInfo() {
        // УДАЛЯЕМ ЗОЛОТУЮ ШАХТУ super(List.of("/buildinggold",
        super(List.of(
                "/buildingmetal",
                "/buildingstone",
                "/buildingwood"));
    }

    @Override
    public Answer respond(Update update) {
        targetBuilding = null;
        userBuilding = null;
        hasBuilding = false;
        Answer answer = new Answer();
        initialQuery(update, true);

        if (!isStatus()) {
            SendMessage sendMessage = getSendMessageFalse();
            answer.setSendMessage(sendMessage);
            return answer;
        }

        if (update.hasCallbackQuery()) {
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
            setBuildingObjects(getQuery());
            answer.setSendPhoto(getSendPhoto());
        }
        return answer;
    }

    private void setBuildingObjects(String query) {
        switch (query) {
            // УДАЛЯЕМ ЗОЛОТУЮ ШАХТУ
            /*
            case "/buildinggold" : {
                targetBuilding = new GoldBuilding();
                setParameters();
                break;
            }

             */
            case "/buildingmetal" : {
                targetBuilding = new MetalBuilding();
                setParameters();
                break;
            }
            case "/buildingstone" : {
                targetBuilding = new StoneBuilding();
                setParameters();
                break;
            }
            case "/buildingwood" : {
                targetBuilding = new WoodBuilding();
                setParameters();
                break;
            }
        }
    }

    private void setParameters() {
        for (BuildingP userBuildingFromDB : getUserDto().getBuildings()) {
            if (targetBuilding.getName().equals(userBuildingFromDB.getName())) {
                userBuilding = userBuildingFromDB;
                hasBuilding = true;
                break;
            }
        }
    }

    @Override
    public InlineKeyboardMarkup getInlineKeyboardMarkup() {
        return getInlineKeyboardMarkup(hasBuilding);
    }

    public SendPhoto getSendPhoto() {
        String img = getImg();
        String targetImg = getTargetImg();
        String text = getText();
        SendPhoto message = sendCustomPhoto(getChatId(), img, targetImg, text);
        message.setReplyMarkup(getInlineKeyboardMarkup(hasBuilding));
        return message;
    }

    @Override
    public String getText() {
        StringBuilder text = new StringBuilder();

        if (hasBuilding) {
            text.append("<b>").append(userBuilding.getName()).append("</b>")
                    .append(": ").append(userBuilding.getLevel()).append(" уровень\n\n");
        } else {
            text.append("<b>").append(targetBuilding.getName()).append("</b>\n");
        }

        text.append("Производство: <i>").append(targetBuilding.getProduction()).append("</i>\n");

        if (hasBuilding) {
            String rateMessage = "";
            int rateBooster = boosterRate(userBuilding);

            if (rateBooster > 0) {
                rateMessage = "Производство ресурса увеличено на " + Formatting.formatWithoutFraction(rateBooster) + " % (ускорители)";
            } else if (rateBooster < 0) {
                rateMessage = "Производство ресурса уменьшено на " + Formatting.formatWithoutFraction(rateBooster) + " % (замедлители)";
            }

            double quantityInHour = resourceService.getQuantityInHour(userBuilding);
            double gettingResourceRate = quantityInHour / 100 * rateBooster;
            text.append("Количество в час: <i>").append(Formatting.formatWithoutFraction(quantityInHour)).append("</i>");

            if (rateBooster > 0) {
                text.append(" <b>+").append(Formatting.formatWithoutFraction(gettingResourceRate)).append("</b>");
            } else if (rateBooster < 0) {
                text.append(" <b>-").append(Formatting.formatWithoutFraction(gettingResourceRate)).append("</b>");
            }

            long timeUpgrade = userBuilding.getLastTimeUpgrade().toEpochMilli() + userBuilding.getTimeToUpdate();
            // если обновление данных случилось без поднятия уровня
            if (userBuilding.getLastUpdate().toEpochMilli() <= timeUpgrade) {
                text.append("\nЗдание улучшается до уровня: <i>").append(userBuilding.getLevel() + 1).append("</i>");
                text.append("\nВремя до улучшения: <i>")
                        .append(buildingService.getDurationToString(timeUpgrade - userBuilding.getLastUpdate().toEpochMilli()))
                        .append("</i>");

                /*
                text.append("\n<b>Стоимость улучшения до ")
                        .append(userBuilding.getLevel() + 2)
                        .append(" уровня:</b>")
                        .append("\n");
                List<Resource> resources = userBuilding.viewPrice(userBuilding.getLevel() + 2);
                for (Resource resource : resources) {
                    text.append(resource.getName()).append(": ")
                            .append(resource.makeQuantityString())
                            .append(" ").append(resource.getEmoji()).append("\n");
                }
                Building futureBuilding = buildingService.cloneBuilding(userBuilding);
                futureBuilding.setLevel(userBuilding.getLevel() + 2);
                text.append("Производство в час: <i>")
                        .append((double) resourceService.getQuantityInHour(futureBuilding)).append("</i>\n");
                text.append("Время строительства: <i>");
                long duration = buildingService.getDuration(resources);
                text.append(buildingService.getDurationToString(duration)).append("</i>\n\n");
                 */

                /*
                text.append("\n<b>Следующие уровни:</b>\n");
                for (int i = Math.max(2, userBuilding.getLevel() + 3); i < userBuilding.getLevel() + 6; i++) {
                    text.append("Необходимо на ").append(i).append(" уровень:").append("\n");
                    List<Resource> resourcesList = targetBuilding.getPrice(i);
                    for (Resource resource : resourcesList) {
                        text.append(resource.getName()).append(": ")
                                .append(resource.getQuantityString())
                                .append(" ").append(resource.getEmoji()).append("\n");
                    }
                }
                 */
            } else {
                text.append("\n\n<b>Стоимость улучшения до ")
                        .append(userBuilding.getLevel() + 1)
                        .append(" уровня:</b>")
                        .append("\n");
                List<ResourceP> resources = userBuilding.viewPrice(userBuilding.getLevel() + 1);
                for (ResourceP resource : resources) {
                    text.append(resource.getName()).append(": ")
                            .append(resource.makeQuantityString())
                            .append(" ").append(resource.getEmoji()).append("\n");
                }
                BuildingP futureBuilding = buildingService.cloneBuilding(userBuilding);
                futureBuilding.setLevel(userBuilding.getLevel() + 1);
                text.append("Производство в час: <i>")
                        .append((int) resourceService.getQuantityInHour(futureBuilding)).append("</i>\n");
                text.append("Время строительства: <i>");
                long duration = buildingService.getDuration(resources);
                text.append(buildingService.getDurationToString(duration)).append("</i>");
                /*
                text.append("\n<b>Следующие уровни:</b>\n");
                for (int i = Math.max(2, userBuilding.getLevel() + 2); i < userBuilding.getLevel() + 5; i++) {
                    text.append("Необходимо на ").append(i).append(" уровень:").append("\n");
                    List<Resource> resourcesList = targetBuilding.getPrice(i);
                    for (Resource resource : resourcesList) {
                        text.append(resource.getName()).append(": ")
                                .append(resource.getQuantityString())
                                .append(" ").append(resource.getEmoji()).append("\n");
                    }
                }
                 */
            }

            // ВЫВОДИМ СООБЩЕНИЕ О ТОМ, ЕСТЬ ЛИ АКТИВНЫЕ БУСТЕРЫ, И СКОЛЬКО ПРОЦЕНТОВ И ЧЕГО ОНИ ДОБАВЛЯЮТ
            if (!rateMessage.isEmpty()) {
                text.append("\n\n").append(rateMessage);
            }
        } else {
            text.append("\nСтоимость строительства:").append("\n");
            List<ResourceP> resources = targetBuilding.viewPrice(1);
            for (ResourceP resource : resources) {
                text.append(resource.getName()).append(": ")
                        .append(resource.makeQuantityString())
                        .append(" ").append(resource.getEmoji()).append("\n");
            }
            text.append("Время строительства:");
            long duration = buildingService.getDuration(resources);
            text.append(buildingService.getDurationToString(duration)).append("\n");
            /*
            text.append("\n<b>Следующие уровни:</b>\n");
            for (int i = 2; i < 6; i++) {
                text.append("Необходимо на ").append(i).append(" уровень:").append("\n");
                List<Resource> resourcesList = targetBuilding.getPrice(i);
                for (Resource resource : resourcesList) {
                    text.append(resource.getName()).append(": ")
                            .append(resource.getQuantityString())
                            .append(" ").append(resource.getEmoji()).append("\n");
                }
            }
            */
        }
        text.append("\n").append(getSpoiler());

        return text.toString();
    }

    public InlineKeyboardMarkup getInlineKeyboardMarkup(boolean hasBuilding) {
        List<Integer> buttonsInLine = List.of(3, 1);
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        buttons.add(getButton(Emoji.ARROW_LEFT.toString(), "/buildings"));
        buttons.add(getButton(Emoji.HOUSE.toString(), "/profile"));
        buttons.add(getButton(Emoji.ARROWS_COUNTERCLOCKWISE.toString(), getQuery()));

        String nameButton;
        if (hasBuilding) {
            nameButton = "Улучшить";
        } else {
            nameButton = "Построить";
        }
        switch (targetBuilding.getName()) {
            // УДАЛЯЕМ ЗОЛОТУЮ ШАХТУ
            //case GOLD_BUILDING -> buttons.add(getButton(nameButton, "/upGold"));
            case WOOD_BUILDING -> buttons.add(getButton(nameButton, "/upWood"));
            case STONE_BUILDING -> buttons.add(getButton(nameButton, "/UpStone"));
            case METAL_BUILDING -> buttons.add(getButton(nameButton, "/upMetal"));
        }
        return getKeyboard(buttonsInLine, buttons);
    }

    private int boosterRate(BuildingP building) {
        long telegramId = getUserDto().getTelegramId();
        long userId = getUserDto().getId();
        List<ResourceType> types = new ArrayList<>(List.of(ResourceType.ACCELERATION_ALL));
        switch (building.getProduction()) {
            case STONE : {
                types.addAll(ResourceType.getStoneBoosters());
                break;
            }
            case METAL : {
                types.addAll(ResourceType.getMetalBoosters());
                break;
            }
            case WOOD : {
                types.addAll(ResourceType.getWoodBoosters());
                break;
            }
            // УДАЛЯЕМ ЗОЛОТУЮ ШАХТУ
                /*
            case GOLD: {
                types.addAll(ResourceType.getGoldBoosters());
            }

                 */
        }

        List<ActiveBoosterP> boosters = DataSet.getBoosterService().findAllABByUserIdAndNameIn(userId, telegramId, types);
        boosters = boosters.stream().filter(booster -> booster.getEndsAt().toEpochMilli() > Instant.now().toEpochMilli()).toList();
        if (boosters.isEmpty()) return 0;

        double rate = 0.0;
        for (ActiveBoosterP booster : boosters) {
            rate += booster.getValue();
        }
        rate *= 100;

        return (int) rate;
    }
}