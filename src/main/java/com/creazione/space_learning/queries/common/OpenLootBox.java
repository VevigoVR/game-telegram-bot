package com.creazione.space_learning.queries.common;

import com.creazione.space_learning.config.DataSet;
import com.creazione.space_learning.game.Item;
import com.creazione.space_learning.enums.ResourceType;
import com.creazione.space_learning.queries.GameCommand;
import com.creazione.space_learning.queries.Query;
import com.creazione.space_learning.utils.Answer;
import com.creazione.space_learning.utils.Formatting;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Component
@GameCommand(
        value = {"/open"},
        description = "Открыть лут бокс"
)
public class OpenLootBox extends Query {
    private String[] args;
    private String wrong = "";
    private List<Item> items;
    private ResourceType resourceType;

    public OpenLootBox() {
        super(List.of());
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

        openLootBox();

        if (items == null) {
            wrong = "Коробки данного типа закончились...";
        }
        SendMessage sendMessage = takeSendMessage();
        if (wrong.isEmpty()) {
            sendMessage.setReplyMarkup(getInlineKeyboardMarkup());
        }
        answer.setSendMessage(sendMessage);
        return answer;
    }

    private SendMessage takeSendMessage() {
        return sendCustomMessage(getChatId(), getText());
    }

    @Override
    public SendPhoto getSendPhoto() {
        return null;
    }

    @Override
    public String getText() {
        if (!wrong.isEmpty()) {
            return wrong;
        } else {
            return "✅ <b>Вы открыли " + resourceType.getName() + ":</b> \n\n" + getGiftToString();
        }
    }

    private String getGiftToString() {
        StringBuilder itemsToString = new StringBuilder();
        for (Item item : items) {
            itemsToString.append(item.getName().getEmoji()).append(" ")
                    .append(item.getName().getName()).append(": +")
                    .append(Formatting.formatWithDots(item.getQuantity()))
                    .append("\n");
        }

        return itemsToString.toString();
    }

    @Override
    public InlineKeyboardMarkup getInlineKeyboardMarkup() {
        List<Integer> buttonsInLine = List.of(1);
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        buttons.add(getButton("Открыть ещё", getQuery()));
        return getKeyboard(buttonsInLine, buttons);
    }

    private void openLootBox() {
        // Извлекаем аргументы команды
        args = getCommandArgsAbsolute("/open");
        if (args == null) {
            wrong = "⚠️ Открыть предмет не удалось " +
                    "\nЧтобы открыть предмет, введите его название: " +
                    "\n<code>/open common</code>," +
                    "\n<code>/open rare</code>...";
        } else if (args[0].equals(ResourceType.LOOT_BOX_COMMON.getMark())) {
            resourceType = ResourceType.LOOT_BOX_COMMON;
            items = DataSet.getLootBoxService().openBox(ResourceType.LOOT_BOX_COMMON, getUserDto());
        } else if (args[0].equals(ResourceType.LOOT_BOX_RARE.getMark())) {
            resourceType = ResourceType.LOOT_BOX_RARE;
            items = DataSet.getLootBoxService().openBox(ResourceType.LOOT_BOX_RARE, getUserDto());
        } else if (args[0].equals("ref")) {
            String[] argsRef = getCommandArgs(args, "ref");

            if (argsRef == null || argsRef[0] == null) {
                wrong = "⚠️ Открыть реферальный предмет не удалось " +
                        "\nЧтобы открыть, введите его название: " +
                        "\n<code>/open ref 1</code>";
            } else if (argsRef[0].equals("1")) {
                resourceType = ResourceType.REFERRAL_BOX_1;
                items = DataSet.getLootBoxService().openBox(resourceType, getUserDto());
            } else if (argsRef[0].equals("2")) {
                resourceType = ResourceType.REFERRAL_BOX_2;
                items = DataSet.getLootBoxService().openBox(resourceType, getUserDto());
            } else if (argsRef[0].equals("3")) {
                resourceType = ResourceType.REFERRAL_BOX_3;
                items = DataSet.getLootBoxService().openBox(resourceType, getUserDto());
            }
        }
    }
}