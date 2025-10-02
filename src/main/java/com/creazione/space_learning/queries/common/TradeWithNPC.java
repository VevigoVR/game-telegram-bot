package com.creazione.space_learning.queries.common;

import com.creazione.space_learning.dto.TransferTradeResult;
import com.creazione.space_learning.entities.game_entity.ResourceDto;
import com.creazione.space_learning.entities.game_entity.UserDto;
import com.creazione.space_learning.enums.Emoji;
import com.creazione.space_learning.game.resources.Gold;
import com.creazione.space_learning.game.resources.Metal;
import com.creazione.space_learning.game.resources.Stone;
import com.creazione.space_learning.queries.GameCommand;
import com.creazione.space_learning.queries.Query;
import com.creazione.space_learning.utils.Answer;
import com.creazione.space_learning.utils.Formatting;
import com.creazione.space_learning.utils.WordUtils;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.List;

@Setter
@Getter
@Component
@GameCommand(
        value = {"/buy", "/sell", "купить", "продать"},
        description = "Продажа/покупка ресурсов у NPC"
)
public class TradeWithNPC extends Query {
    private static final Pattern COMMAND_PATTERN = Pattern.compile(
            "^(/sell|/buy|продать|купить)\\s+(gold|metal|stone|золото|металл|камень)\\s+(\\d+)([кkмm]*)$",
            Pattern.CASE_INSENSITIVE
    );
    //private String quantityOfResource = "";
    //private String wrong = "";
    //private TransferTradeResult tradeResult;

    public TradeWithNPC() {
        super(List.of());
    }

    @Override
    public Answer respond(Update update) {
        tradeResult = new TransferTradeResult();
        tradeResult.setTransferred(true);
        quantityOfResource = ""; // чтобы не сохранялся старый результат
        wrong = "";
        Answer answer = new Answer();
        initialQuery(update, false);

        if (!isStatus()) {
            SendMessage sendMessage = getSendMessageFalse();
            answer.setSendMessage(sendMessage);
            return answer;
        }

        parseAndExecute(getQuery(), getUserDto());
        if (!tradeResult.isTransferred()) {
            wrong = tradeResult.getMessage();
        }

        SendMessage sendMessage = takeSendMessage();
        answer.setSendMessage(sendMessage);
        return answer;
    }

    public void parseAndExecute(String command, UserDto userDto) {
        try {
            Matcher matcher = COMMAND_PATTERN.matcher(command.trim());

            if (!matcher.matches()) {
                getText();
                tradeResult = new TransferTradeResult("");
            }

            String action = matcher.group(1);//.substring(1); // Убираем "/"
            String resourceType = matcher.group(2).toLowerCase();
            String numberStr = matcher.group(3);
            String suffixes = matcher.group(4).toLowerCase();

            // Парсим базовое число
            long baseValue = Long.parseLong(numberStr);

            if (baseValue <= 0) {
                throw new IllegalArgumentException("Количество должно быть положительным числом \uD83D\uDE09");
            }

            // Обрабатываем суффиксы
            long multiplier = 1;
            for (char suffix : suffixes.toCharArray()) {
                if (suffix == 'k' || suffix == 'к') {
                    multiplier *= 1000;
                } else if (suffix == 'm' || suffix == 'м') {
                    multiplier *= 1000000;
                }
            }

            long quantity = baseValue * multiplier;

            if (quantity < 100) {
                throw new IllegalArgumentException("Продать/купить можно только оптом от 100 ресурсов!");
            }

            // Проверяем на переполнение
            if (quantity / multiplier != baseValue) {
                throw new IllegalArgumentException("Слишком большое значение");
            }

            // Выбираем соответствующий ресурс
            ResourceDto resource;
            switch (resourceType) {
                case "gold", "золото":
                    resource = new Gold(quantity);
                    break;
                case "metal", "металл":
                    resource = new Metal(quantity);
                    break;
                case "stone", "камень":
                    resource = new Stone(quantity);
                    break;
                default:
                    throw new IllegalArgumentException("Неизвестный тип ресурса: " + resourceType);
            }

            // Вызываем соответствующий метод сервиса
            if ("/sell".equals(action)) {
                tradeResult = resourceService.sellResource(resource, userDto);
            } else if ("/buy".equals(action)) {
                tradeResult = resourceService.buyResource(resource, userDto);
            } else if ("продать".equals(action)) {
                tradeResult = resourceService.sellResource(resource, userDto);
            } else if ("купить".equals(action)) {
                tradeResult = resourceService.buyResource(resource, userDto);
            } else {
                getText();
            }

        } catch (NumberFormatException e) {
            if (e.getMessage().startsWith("For")) {
                wrong = "Какие-то странные у вас числа!";
            }
            // wrong = e.getMessage(); //"Невозможно разобрать количество ресурсов!";
            //System.out.println("wrong NumberFormatException: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            wrong = e.getMessage();
            //System.out.println("wrong IllegalArgumentException: " + e.getMessage());
        } catch (Exception e) {
            wrong = "Не понимаю запрос, возможно неверный тип ресурса";
        }
    }

    @Override
    public String getText() {
        String message;
        if (!tradeResult.isTransferred() && !tradeResult.getMessage().isEmpty()) {
            message = Emoji.EXCLAMATION + " " + tradeResult.getMessage();
        } else if (wrong != null && !wrong.isEmpty()) {
            message = Emoji.EXCLAMATION + " " + wrong + "\n\n" + getWrong();
        } else {
            if (!tradeResult.isBuy()) {
                message = "Поздравляю! Вы успешно продали "
                        + tradeResult.getUserResource().getEmoji() + " "
                        + tradeResult.getUserResource().getName() + " в размере "
                        + Formatting.formatWithDots(tradeResult.getUserResource().getQuantity()) + " шт. и заработали "
                        + tradeResult.getNpcResource().getEmoji() + " "
                        + tradeResult.getNpcResource().getName() + ": "
                        + Formatting.formatWithDots(tradeResult.getNpcResource().getQuantity()) + " "
                        + WordUtils.rightWord(tradeResult.getNpcResource().getQuantity(), "единицу", "единицы", "единиц");
            } else {
                message = "Поздравляю! Вы успешно купили "
                        + tradeResult.getNpcResource().getEmoji() + " "
                        + tradeResult.getNpcResource().getName() + " в размере "
                        + Formatting.formatWithDots(tradeResult.getNpcResource().getQuantity()) + " "
                        + WordUtils.rightWord(tradeResult.getNpcResource().getQuantity(), "единицу", "единицы", "единиц") + " за "
                        + tradeResult.getUserResource().getEmoji() + " "
                        + tradeResult.getUserResource().getName() + ": "
                        + Formatting.formatWithDots(tradeResult.getUserResource().getQuantity())
                        + " шт.";
            }
        }
        return message;
    }

    public String getWrong() {
        return """
            Используйте:
            /sell metal|stone количество[k|m]
            /buy metal|stone количество[k|m]
            
            Примеры:
            <code>/sell metal 100</code>
            <code>/buy metal 5k</code>
            <code>/buy stone 5kk</code>
            <code>/sell stone 10mk</code>
            
            Суффиксы:
            к/k - умножить на 1000 (кило)
            м/m - умножить на 1000000 (мега)
            Можно использовать несколько суффиксов: 1kk = 1 000 000
            """;
    }

    @Override
    public InlineKeyboardMarkup getInlineKeyboardMarkup() {
        return null;
    }

    @Override
    public SendPhoto getSendPhoto() {
        return null;
    }

    private SendMessage takeSendMessage() {
        return sendCustomMessage(getChatId(), getText());
    }
}
