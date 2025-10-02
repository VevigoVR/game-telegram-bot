package com.creazione.space_learning.queries.common;

import com.creazione.space_learning.config.DataSet;
import com.creazione.space_learning.entities.game_entity.UserDto;
import com.creazione.space_learning.entities.postgres.ActiveBoosterP;
import com.creazione.space_learning.entities.postgres.InventoryBoosterP;
import com.creazione.space_learning.enums.ResourceType;
import com.creazione.space_learning.queries.GameCommand;
import com.creazione.space_learning.queries.Query;
import com.creazione.space_learning.utils.Answer;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Log4j2
@Component
@GameCommand(
        value = {"/activate"},
        description = "Активировать бустер"
)
public class ActivateBooster extends Query {
    //private String wrong = "";

    public ActivateBooster() {
        super(List.of());
    }

    private SendMessage takeSendMessage() {
        return sendCustomMessage(getChatId(), getText());
    }

    @Override
    public Answer respond(Update update) {
        wrong = "";

        Answer answer = new Answer();
        initialQuery(update, false);

        if (!isStatus()) {
            SendMessage sendMessage = getSendMessageFalse();
            answer.setSendMessage(sendMessage);
            return answer;
        }

        if (update.hasCallbackQuery()) {
            answer.setAnswerCallbackQuery(closeRespond(update));
        }

        activateBooster();

        if (false) {
            wrong = "Боксы данного типа закончились...";
        }
        SendMessage sendMessage = takeSendMessage();
        answer.setSendMessage(sendMessage);
        return answer;
    }

    @Override
    public InlineKeyboardMarkup getInlineKeyboardMarkup() {
        return null;
    }

    @Override
    public String getText(UserDto userDto) {
        if (!wrong.isEmpty()) {
            return wrong;
        } else {
            return "✅ <b>Вы успешно активировали бустер!</b>";
        }
    }

    @Override
    public SendPhoto getSendPhoto() {
        return null;
    }

    private void activateBooster() {
        String[] queryArray = getQuery().split("\\s+");
        // Извлекаем аргументы команды
        // Предположим, изначально был запрос: /activate acceleration all 50 24
        if (queryArray.length != 5) {
            wrong = "⚠️ Неправильно введена команда, поищите подсказку на складе!";
            return;
        }
        switch (queryArray[0] + " " + queryArray[1]) {
            case "/activate acceleration": {
                try {
                    activateAcceleration(queryArray[2], Integer.parseInt(queryArray[3]), Integer.parseInt(queryArray[4]));
                } catch (NumberFormatException exception) {
                    wrong = "⚠️ Неправильно введена команда, поищите подсказку на складе!!!";
                }
                break;
            }
            default:
                wrong = "⚠️ Неправильно введена команда, поищите подсказку на складе!!";
        }
    }

    private void activateAcceleration(String type, int rate, int time) {
        switch (type) {
            case "all": {
                activateAcceleration(ResourceType.ACCELERATION_ALL, rate, time);
                break;
            }
            case "metal": {
                activateAcceleration(ResourceType.ACCELERATION_METAL, rate, time);
                break;
            }
            case "stone": {
                activateAcceleration(ResourceType.ACCELERATION_STONE, rate, time);
                break;
            }
            // УДАЛЯЕМ ЗОЛОТУЮ ШАХТУ И ЛЕСОПИЛКУ
                /*
            case "wood": {
                activateAcceleration(ResourceType.ACCELERATION_WOOD, rate, time);
                break;
            }
            case "gold": {
                activateAcceleration(ResourceType.ACCELERATION_GOLD, rate, time);
                break;
            }
                 */
            default: return;
        }
    }

    private void activateAcceleration(ResourceType resourceType, int rate, int time) {
        double value = ((double) rate) / 100;
        Set<InventoryBoosterP> boosterSet = DataSet.getBoosterService()
                .findAllIBByUserIdAndNameAndValueAndDurationMilli(
                        getUserDto().getId(),
                        getUserDto().getTelegramId(),
                        resourceType,
                        value,
                        (((long)time) * 3600000L)
                );
        if (boosterSet == null || boosterSet.isEmpty()) {
            wrong = "⚠️ На вашем складе нет данного вида ускорителя, попробуйте поискать другой.";
            return;
        }
        if (boosterSet.size() > 1) {
            //log.error("ресурс имеет дубликат, всего одинаковых: " + boosterSet.size());
            wrong = "⚠️ Ошибка на стороне сервера, пожалуйста, попробуйте позже.";
            return;
        }
        Optional<InventoryBoosterP> inventoryBooster = boosterSet.stream().findFirst();
        InventoryBoosterP inventoryB = inventoryBooster.get();
        inventoryB.setQuantity(inventoryB.getQuantity() - 1);
        if (inventoryB.getQuantity() <= 0) {
            DataSet.getBoosterService().deleteIB(inventoryB, getUserDto().getTelegramId());
        } else {
            DataSet.getBoosterService().saveIB(inventoryB, getUserDto().getTelegramId());
        }

        ActiveBoosterP activeBooster = new ActiveBoosterP(getUserDto().getId(), resourceType, value, time);
        DataSet.getBoosterService().saveAB(activeBooster, getUserDto().getTelegramId(), getUserDto().getId());
    }
}