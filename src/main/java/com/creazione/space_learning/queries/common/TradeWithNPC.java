package com.creazione.space_learning.queries.common;

import com.creazione.space_learning.dto.TransferResult;
import com.creazione.space_learning.entities.game_entity.ResourceDto;
import com.creazione.space_learning.entities.game_entity.UserDto;
import com.creazione.space_learning.enums.Emoji;
import com.creazione.space_learning.game.resources.Gold;
import com.creazione.space_learning.game.resources.Metal;
import com.creazione.space_learning.game.resources.Stone;
import com.creazione.space_learning.queries.GameCommand;
import com.creazione.space_learning.queries.Query;
import com.creazione.space_learning.utils.Answer;
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
    private String quantityOfResource = "";
    private String wrong = "";

    public TradeWithNPC() {
        super(List.of());
    }

    @Override
    public Answer respond(Update update) {
        quantityOfResource = ""; // чтобы не сохранялся старый результат
        wrong = "";
        Answer answer = new Answer();
        initialQuery(update, false);

        if (!isStatus()) {
            SendMessage sendMessage = getSendMessageFalse();
            answer.setSendMessage(sendMessage);
            return answer;
        }

        TransferResult transferResult = parseAndExecute(getQuery(), getUserDto());

        SendMessage sendMessage = takeSendMessage();
        answer.setSendMessage(sendMessage);
        return answer;
    }

    public TransferResult parseAndExecute(String command, UserDto userDto) {
        try {
            Matcher matcher = COMMAND_PATTERN.matcher(command.trim());

            if (!matcher.matches()) {
                getText();
                return new TransferResult(1L, 1.0);
            }

            String action = matcher.group(1).substring(1); // Убираем "/"
            String resourceType = matcher.group(2).toLowerCase();
            String numberStr = matcher.group(3);
            String suffixes = matcher.group(4).toLowerCase();

            // Парсим базовое число
            long baseValue = Long.parseLong(numberStr);

            if (baseValue <= 0) {
                throw new IllegalArgumentException("Количество должно быть положительным числом");
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
            if ("sell".equals(action)) {
                return resourceService.sellResource(resource, userDto);
            } else if ("buy".equals(action)) {
                return resourceService.buyResource(resource, userDto);
            } else {
                getText();
            }

        } catch (NumberFormatException e) {
            wrong = "Неверный числовой формат: " + e.getMessage();
        } catch (IllegalArgumentException e) {
            wrong = e.getMessage();
        } catch (Exception e) {
            wrong = "Произошла ошибка при обработке команды: ";
        }
        return null;
    }

    @Override
    public String getText() {
        String message = Emoji.EXCLAMATION + " " + """
            Неверный формат команды. Используйте:
            /sell gold|metal|stone количество[k|m]
            /buy gold|metal|stone количество[k|m]
            
            Примеры:
            /sell gold 100
            /buy metal 5k
            /sell stone 10m
            
            Суффиксы:
            к/k - умножить на 1000 (кило)
            м/m - умножить на 1000000 (мега)
            Можно использовать несколько суффиксов: 1kk = 1 000 000
            """;

        if (wrong != null && !wrong.isEmpty()) {
            message = Emoji.EXCLAMATION + " " + wrong + "\n\n" + message;
        } else {
            message = """
                    
                    """;
        }
        return message;
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
