package com.creazione.space_learning.queries.common;

import com.creazione.space_learning.config.DataSet;
import com.creazione.space_learning.dto.UnreadNoticeInfo;
import com.creazione.space_learning.dto.UserInitialDto;
import com.creazione.space_learning.entities.game_entity.ResourceDto;
import com.creazione.space_learning.entities.game_entity.UserDto;
import com.creazione.space_learning.entities.postgres.AggregateNoticeP;
import com.creazione.space_learning.enums.NoticeType;
import com.creazione.space_learning.queries.GameCommand;
import com.creazione.space_learning.queries.Query;
import com.creazione.space_learning.service.scheduler.SchedulerService;
import com.creazione.space_learning.utils.Answer;
import com.creazione.space_learning.enums.Emoji;
import com.creazione.space_learning.utils.Formatting;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
@GameCommand(
        value = {"/post", "почта", "уведомления", "/postnw"},
        description = "Просмотр уведомлений игрока"
)
public class PostCenter extends Query {
    public PostCenter() {
        super(List.of());
    }

    @Override
    public Answer respond(Update update) {
        Answer answer = new Answer();
        UserInitialDto userInitialDto = initialQuery(update, true);

        if (!userInitialDto.isStatus()) {
            SendMessage sendMessage = getSendMessageFalse(userInitialDto.getChatId());
            answer.setSendMessage(sendMessage);
            return answer;
        }

        if (update.hasCallbackQuery()) {
            answer.setAnswerCallbackQuery(closeRespond(update));
            if (update.getCallbackQuery().getData().equals("/postnw")) {
                answer.setSendPhoto(getSendPhoto(userInitialDto, null));
                return answer;
            }
            EditMessageCaption newText = EditMessageCaption.builder()
                    .chatId(userInitialDto.getChatId())
                    .messageId(userInitialDto.getMessageId())
                    .build();
            newText.setReplyMarkup(getInlineKeyboardMarkup(userInitialDto, null));
            newText.setCaption(getText(userInitialDto, null));
            newText.setParseMode(ParseMode.HTML);
            answer.setEditMessageCaption(newText);
        } else {
            answer.setSendPhoto(getSendPhoto(userInitialDto, null));
        }
        return answer;
    }

    @Override
    public SendPhoto getSendPhoto(UserInitialDto userInitialDto, Object noObject) {
        String img = getImg();
        String text = getText(userInitialDto, null);
        SendPhoto message = sendCustomPhoto(userInitialDto.getChatId(), img, getTargetImg(), text);
        message.setReplyMarkup(getInlineKeyboardMarkup(userInitialDto, null));
        return message;
    }

    @Override
    public String getText(UserInitialDto userInitialDto, Object noObject) {
        UserDto userDto = userInitialDto.getUserDto();
        StringBuilder text = new StringBuilder();
        UnreadNoticeInfo unreadNoticeInfo = DataSet
                .getAggregateNoticeService()
                .findLatestUnreadNoticeWithHasMoreFlag(userDto.getId());
        AggregateNoticeP aggregateNoticeEntity = unreadNoticeInfo.getLatestNotice();
        text.append("<b>Центр сообщений</b>\n\n");
        if (aggregateNoticeEntity == null) {
            text.append("Сообщений пока нет\n\n")
                    .append(Emoji.POST_BOX)
                    .append(" <code>Отсутствие новостей — уже хорошая новость!</code>");
        } else {
            createTitleAndText(userInitialDto, aggregateNoticeEntity);
            LocalDateTime ldt = LocalDateTime.ofInstant(aggregateNoticeEntity.getCreatedAt().toInstant(), ZoneId.systemDefault());
            text.append(Emoji.ARROW_RIGHT).append(" ")
                    .append(aggregateNoticeEntity.getTitle())
                    .append("\n\n")
                    .append(Emoji.TEXT).append(" ")
                    .append(aggregateNoticeEntity.getText())
                    .append("\n Сообщение отправлено: ")
                    .append(formatDate(ldt));
            aggregateNoticeEntity.setRead(true);
            userDto.setPost(unreadNoticeInfo.isHasMoreUnread());
            DataSet.getUserService().saveFull(userDto);
            DataSet.getAggregateNoticeService().save(aggregateNoticeEntity);
        }
        text.append("\n").append(getSpoiler());
        return text.toString();
    }

    private String formatDate(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }

    private void createTitleAndText(UserInitialDto userInitialDto, AggregateNoticeP aggregateNoticeEntity) {
        UserDto userDto = userInitialDto.getUserDto();
        StringBuilder text = new StringBuilder();
        if (aggregateNoticeEntity.getNoticeType().equals(NoticeType.GIFT_TO_REFERRER)) {
            aggregateNoticeEntity.setTitle("Награда за активных рефералов".toUpperCase());
            text.append("За участие и активность Ваших приглашённых соратников Вы получаете:\n\n");
        } else if (aggregateNoticeEntity.getNoticeType().equals(NoticeType.GIFT_TO_REFERRAL)) {
            aggregateNoticeEntity.setTitle("Награда за активность в команде".toUpperCase());
            text.append("За участие и активность в команде Вы получаете:\n\n");
        }
        List<ResourceDto> resources = SchedulerService.convertToResources(aggregateNoticeEntity.getResources());
        SchedulerService.addOrIncrementResource(userDto.getResources(), resources, userDto.getId());
        for (ResourceDto resource : resources) {
            text.append(resource.getEmoji())
                    .append(" ")
                    .append(resource.getName())
                    .append(": ")
                    .append(Formatting.formatWithDots(resource.getQuantity()))
                    .append("\n");
        }
        aggregateNoticeEntity.setText(text.toString());
    }

    @Override
    public InlineKeyboardMarkup getInlineKeyboardMarkup(UserInitialDto userInitialDto, Object noObject) {
        List<Integer> buttonsInLine = List.of(3);
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        buttons.add(getButton(Emoji.OUTBOX_TRAY.toString(), "/post"));
        buttons.add(getButton(Emoji.HOUSE.toString(), "/profile"));
        buttons.add((getButton(Emoji.EJECT_SYMBOL.toString(), "/postnw")));
        return getKeyboard(buttonsInLine, buttons);
    }
}
