package com.creazione.space_learning.queries.common;

import com.creazione.space_learning.dto.PaginationDto;
import com.creazione.space_learning.entities.postgres.InventoryBoosterP;
import com.creazione.space_learning.queries.GameCommand;
import com.creazione.space_learning.queries.Query;
import com.creazione.space_learning.utils.Answer;
import com.creazione.space_learning.enums.Emoji;
import com.creazione.space_learning.utils.WordUtils;
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
        value = {"/boosters", "бустеры", "/boostersnw"},
        description = "Просмотр склада/ресурсов игрока"
)
public class StorageBoosters extends Query {
    private PaginationDto paginationDto;
    String queryWithoutPage;

    public StorageBoosters() {
        super(List.of());
    }

    @Override
    public Answer respond(Update update) {
        paginationDto = new PaginationDto();
        paginationDto.setPage(1);
        paginationDto.setLimit(3);
        Answer answer = new Answer();
        initialQuery(update, true);

        if (!isStatus()) {
            SendMessage sendMessage = getSendMessageFalse();
            answer.setSendMessage(sendMessage);
            return answer;
        }

        //System.out.println("page: " + paginationDto.getPage());
        String[] args = getCommandArgsAbsolute(List.of("/boosters", "бустеры", "/boostersnw"));
        if (args != null && args.length > 1) {
            setPage(args[1]);
            //System.out.println("page после инициализации: " + paginationDto.getPage());
        }
        if (args != null) {
            queryWithoutPage = args[0];
            //System.out.println("длина args: " + args.length);
        }
        if (!getUserDto().viewSortedBoosters().isEmpty()) {
            paginationDto.setSize(getUserDto().viewSortedBoosters().size());
        }
        insertPagination(paginationDto, queryWithoutPage);

        if (update.hasCallbackQuery()) {
            answer.setAnswerCallbackQuery(closeRespond(update));
            if (update.getCallbackQuery().getData().startsWith("/boostersnw")) {
                answer.setSendPhoto(getSendPhoto());
                return answer;
            }
            EditMessageCaption newText = EditMessageCaption.builder()
                    .chatId(getChatId())
                    .messageId(getMessageId())
                    .build();
            newText.setReplyMarkup(getInlineKeyboardMarkup());
            newText.setCaption(getText());
            newText.setParseMode(ParseMode.HTML);
            answer.setEditMessageCaption(newText);
        } else {
            answer.setSendPhoto(getSendPhoto());
        }
        return answer;
    }

    @Override
    public SendPhoto getSendPhoto() {
        String img = getImg();
        String text = getText();
        SendPhoto message = sendCustomPhoto(getChatId(), img, getTargetImg(), text);
        message.setReplyMarkup(getInlineKeyboardMarkup());
        return message;
    }

    @Override
    public String getText() {
        StringBuilder text = new StringBuilder();
        text.append("<b>Склад ").append(getUserDto().getName()).append("</b>\n\n<b>Ускорители</b>:\n");
        if (!getUserDto().viewSortedBoosters().isEmpty()) {
            List<InventoryBoosterP> boosters = getUserDto().viewSortedBoosters();
            int i = 0;
            if (paginationDto.getSize() > paginationDto.getLimit()) {
                if (paginationDto.getPage() > 1) {
                    i = i - (paginationDto.getLimit() * (paginationDto.getPage() - 1));
                }
            }

            for (InventoryBoosterP booster : boosters) {
                if (i < 0 || i >= paginationDto.getLimit()) {
                    i++;
                    continue;
                }
                text.append(booster.getName().getEmoji()).append(" <b>").append(booster.getName())
                        .append("</b> на ").append((int) (booster.getValue()*100)).append("% на ")
                        .append(booster.getDurationMilli()/3600000).append(" ").append(WordUtils.rightWord(booster.getDurationMilli()/3600000, "час", "часа", "часов"))
                        .append(": ")
                        .append(booster.makeQuantityString())
                        .append(" шт.\n")
                        .append("<b>активировать: </b><code>")
                        .append(booster.getName().getMark()).append(" ")
                        .append((int) (booster.getValue()*100)).append(" ")
                        .append(booster.getDurationMilli()/3600000).append("</code>\n");
                i++;
            }
        } else {
            text.append("<i>на складе не найдено ускорителей...</i>\n");
        }
        if (paginationDto.getTotalPages() > 1) {
            text.append("\n<i>Страница: ").append(paginationDto.getPage()).append(" из ").append(paginationDto.getTotalPages()).append("</i>\n");
        }
        text.append(getSpoiler());
        return text.toString();
    }

    @Override
    public InlineKeyboardMarkup getInlineKeyboardMarkup() {
        int numButtons = 2;
        List<InlineKeyboardButton> paginationButtons = paginationDto.getButtons();
        if (!paginationButtons.isEmpty()) {
            numButtons += paginationButtons.size();
        }
        List<Integer> buttonsInLine = List.of(numButtons, 3);

        List<InlineKeyboardButton> buttons = new ArrayList<>();
        buttons.add(getButton(Emoji.HOUSE.toString(), "/profile"));
        buttons.addAll(paginationButtons);
        buttons.add((getButton(Emoji.EJECT_SYMBOL.toString(), "/boostersnw " + paginationDto.getPage())));
        buttons.add(getButton("Ресурсы", "/resources"));
        buttons.add(getButton("Коробки", "/lootboxes"));
        buttons.add(getButton(Emoji.ARROWS_COUNTERCLOCKWISE.toString(), "/boosters " + paginationDto.getPage()));
        return getKeyboard(buttonsInLine, buttons);
    }

    private void setPage(String arg) {
        try {
            paginationDto.setPage(Integer.parseInt(arg));
        } catch (Exception e) {
            //System.out.println("Сработало исключение парсинга arg");
        }
    }
}