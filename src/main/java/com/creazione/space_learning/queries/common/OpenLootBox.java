package com.creazione.space_learning.queries.common;

import com.creazione.space_learning.config.DataSet;
import com.creazione.space_learning.dto.OpenLootBoxDto;
import com.creazione.space_learning.dto.UserInitialDto;
import com.creazione.space_learning.entities.game_entity.UserDto;
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
public class OpenLootBox extends Query<OpenLootBoxDto> {
    //private String[] args;
    //private String wrong = "";
    //private List<Item> items;
    //private ResourceType resourceType;

    public OpenLootBox() {
        super(List.of());
    }

    @Override
    public Answer respond(Update update) {
        Answer answer = new Answer();
        UserInitialDto userInitialDto = initialQuery(update, false);

        if (!userInitialDto.isStatus()) {
            SendMessage sendMessage = getSendMessageFalse(userInitialDto.getChatId());
            answer.setSendMessage(sendMessage);
            return answer;
        }

        OpenLootBoxDto openLootBoxDto = new OpenLootBoxDto();

        if (update.hasCallbackQuery()) {
            answer.setAnswerCallbackQuery(closeRespond(update));
        }

        openLootBox(userInitialDto, openLootBoxDto);

        if (openLootBoxDto.getItems() == null) {
            openLootBoxDto.setWrong("Коробки данного типа закончились...");
        }
        SendMessage sendMessage = takeSendMessage(userInitialDto, openLootBoxDto);
        if (openLootBoxDto.getWrong().isEmpty()) {
            sendMessage.setReplyMarkup(getInlineKeyboardMarkup(userInitialDto, openLootBoxDto));
        }
        answer.setSendMessage(sendMessage);
        return answer;
    }

    private SendMessage takeSendMessage(UserInitialDto userInitialDto, OpenLootBoxDto openLootBoxDto) {
        return sendCustomMessage(userInitialDto.getChatId(), getText(userInitialDto, openLootBoxDto));
    }

    @Override
    public SendPhoto getSendPhoto(UserInitialDto userInitialDto, OpenLootBoxDto openLootBoxDto) {
        return null;
    }

    @Override
    public String getText(UserInitialDto userInitialDto, OpenLootBoxDto openLootBoxDto) {
        String wrong = openLootBoxDto.getWrong();
        if (!wrong.isEmpty()) {
            return wrong;
        } else {
            return "✅ <b>Вы открыли " + openLootBoxDto.getResourceType().getName() + ":</b> \n\n" + getGiftToString(openLootBoxDto);
        }
    }

    private String getGiftToString(OpenLootBoxDto openLootBoxDto) {
        StringBuilder itemsToString = new StringBuilder();
        for (Item item : openLootBoxDto.getItems()) {
            itemsToString.append(item.getName().getEmoji()).append(" ")
                    .append(item.getName().getName()).append(": +")
                    .append(Formatting.formatWithDots(item.getQuantity()))
                    .append("\n");
        }

        return itemsToString.toString();
    }

    @Override
    public InlineKeyboardMarkup getInlineKeyboardMarkup(UserInitialDto userInitialDto, OpenLootBoxDto openLootBoxDto) {
        List<Integer> buttonsInLine = List.of(1);
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        buttons.add(getButton("Открыть ещё", userInitialDto.getQuery()));
        return getKeyboard(buttonsInLine, buttons);
    }

    private void openLootBox(UserInitialDto userInitialDto, OpenLootBoxDto openLootBoxDto) {
        UserDto userDto = userInitialDto.getUserDto();
        // Извлекаем аргументы команды
        openLootBoxDto.setArgs(getCommandArgsAbsolute(userInitialDto, "/open"));
        String[] args = openLootBoxDto.getArgs();
        if (args == null) {
            openLootBoxDto.setWrong("⚠️ Открыть предмет не удалось " +
                    "\nЧтобы открыть предмет, введите его название: " +
                    "\n<code>/open common</code>," +
                    "\n<code>/open rare</code>...");
        } else if (args[0].equals(ResourceType.LOOT_BOX_COMMON.getMark())) {
            openLootBoxDto.setResourceType(ResourceType.LOOT_BOX_COMMON);
            openLootBoxDto.setItems(DataSet.getLootBoxService().openBox(ResourceType.LOOT_BOX_COMMON, userDto));
        } else if (args[0].equals(ResourceType.LOOT_BOX_RARE.getMark())) {
            openLootBoxDto.setResourceType(ResourceType.LOOT_BOX_RARE);
            openLootBoxDto.setItems(DataSet.getLootBoxService().openBox(ResourceType.LOOT_BOX_RARE, userDto));
        } else if (args[0].equals("ref")) {
            String[] argsRef = getCommandArgs(args, "ref");

            if (argsRef == null || argsRef[0] == null) {
                openLootBoxDto.setWrong("⚠️ Открыть реферальный предмет не удалось " +
                        "\nЧтобы открыть, введите его название: " +
                        "\n<code>/open ref 1</code>");
            } else if (argsRef[0].equals("1")) {
                openLootBoxDto.setResourceType(ResourceType.REFERRAL_BOX_1);
                openLootBoxDto.setItems(DataSet.getLootBoxService().openBox(openLootBoxDto.getResourceType(), userDto));
            } else if (argsRef[0].equals("2")) {
                openLootBoxDto.setResourceType(ResourceType.REFERRAL_BOX_2);
                openLootBoxDto.setItems(DataSet.getLootBoxService().openBox(openLootBoxDto.getResourceType(), userDto));
            } else if (argsRef[0].equals("3")) {
                openLootBoxDto.setResourceType(ResourceType.REFERRAL_BOX_3);
                openLootBoxDto.setItems(DataSet.getLootBoxService().openBox(openLootBoxDto.getResourceType(), userDto));
            }
        }
    }
}