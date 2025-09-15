package com.creazione.space_learning.queries;

import com.creazione.space_learning.config.DataSet;
import com.creazione.space_learning.dto.UserDto;
import com.creazione.space_learning.entities.UserEntity;
import com.creazione.space_learning.service.scheduler.SchedulerService;
import com.creazione.space_learning.service.BuildingService;
import com.creazione.space_learning.service.ReferralService;
import com.creazione.space_learning.service.ResourceService;
import com.creazione.space_learning.service.postgres.UserService;
import com.creazione.space_learning.utils.Answer;
import lombok.Getter;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public abstract class Query {
    protected ResourceService resourceService;
    protected BuildingService buildingService;
    protected UserService userService;
    protected ReferralService referralService;

    private  List<String> queries;
    private String query;
    private final String img = "/static/image/profile.jpeg";
    private final String targetImg = "profile.jpeg";
    private long chatId;
    private int messageId;
    private String userName;
    private volatile boolean status = false;
    private UserDto userDto;

    public Query(List<String> query) {
        this.resourceService = DataSet.getResourceService();
        this.buildingService = DataSet.getBuildingService();
        this.userService = DataSet.getUserService();
        this.referralService = DataSet.getReferralService();
        this.queries = query;
    }

    public void initialQuery(Update update, boolean isUpdate) {
        if (update.hasCallbackQuery()) {
            setQuery(update.getCallbackQuery().getData().toLowerCase().trim());
            setChatId(update.getCallbackQuery().getMessage().getChatId());
            setUserName(update.getCallbackQuery().getFrom().getUserName());
            setMessageId(update.getCallbackQuery().getMessage().getMessageId());
            findInitialQuery(isUpdate);
        } else {
            setQuery(update.getMessage().getText().toLowerCase().trim());
            setChatId(update.getMessage().getChatId());
            findInitialQuery(isUpdate);
        }
    }

    public void findInitialQuery(boolean isUpdate) {
        try {
            UserEntity user = getUserEntityFromDB();
            if (user != null) {
                setUserDto(user.convertToUserDto());
                setStatus(true);
                if (isUpdate) {
                    boolean isUpdateDBNow = updateResourcesAndBuildings();
                    if (isUpdateDBNow) {
                        userService.saveFull(getUserDto().convertToUserEntity());
                    }
                }
            }
        } catch (Exception exception) {
            System.out.println("Ошибка доступа пользователя по методу: findInitialQuery(boolean isUpdate): " + Query.class);
            exception.printStackTrace();
        }
    }

    public abstract Answer respond(Update update);

    public SendPhoto sendCustomPhoto(Long chatId, String img, String targetImg, String text) {
        InputFile inputFile = new InputFile(getClass().getResourceAsStream(img), targetImg);
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId);
        sendPhoto.setParseMode(ParseMode.HTML);
        sendPhoto.setPhoto(inputFile);
        sendPhoto.setCaption(text);
        return sendPhoto;
    }

    public SendMessage sendCustomMessage(Long chatId, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setParseMode(ParseMode.HTML);
        sendMessage.disableWebPagePreview();
        sendMessage.setChatId(chatId);
        sendMessage.enableHtml(true);
        sendMessage.setText(text);
        return sendMessage;
    }

    public InlineKeyboardMarkup getKeyboard(List<Integer> rowsInLine, List<InlineKeyboardButton> buttons) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        int key = 0;
        for (int i = 0; i < rowsInLine.size(); i++) {
            List<InlineKeyboardButton> buttonsInLine = new ArrayList<>();
            for (int row = 0; row < rowsInLine.get(i); row++) {
                buttonsInLine.add(buttons.get(key));
                key++;
            }
            rowsInline.add(buttonsInLine);
        }
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    public InlineKeyboardButton getButton(String text, String data) {
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButton.setText(text);
        inlineKeyboardButton.setCallbackData(data);
        return inlineKeyboardButton;
    }

    public AnswerCallbackQuery closeRespond(Update update) {
        String callId = update.getCallbackQuery().getId();
        return AnswerCallbackQuery
                .builder()
                .callbackQueryId(callId)
                .build();
    }

    protected EditMessageReplyMarkup editMessageReplyMarkup() {
        return EditMessageReplyMarkup.builder()
                .chatId(String.valueOf(getChatId()))
                .messageId(getMessageId())
                .build();
    }

    protected EditMessageText editMessageText(String text) {
        return EditMessageText.builder()
                .chatId(getChatId())
                .messageId(getMessageId())
                .text(text)
                .build();
    }

    public UserEntity getUserEntityFromDB() {
        return userService.findFullUserByTelegramId(getChatId());
    }

    public SendMessage getSendMessageFalse() {
        return new SendMessage(String.valueOf(getChatId()), "Пользователь не найден. \nДля регистрации: /start");
    }

    public Answer getCommonRespond(Update update, boolean isUpdate) {
        Answer answer = new Answer();
        initialQuery(update, isUpdate);
        if (!isStatus()) {
            SendMessage sendMessage = getSendMessageFalse();
            answer.setSendMessage(sendMessage);
            return answer;
        }



        if (update.hasCallbackQuery()) {
            answer.setAnswerCallbackQuery(closeRespond(update));

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

    protected boolean updateResourcesAndBuildings() {
        UserDto userDto = getUserDto();
        if (userDto.isSuperAggregate()) {
            toGrant();
            resourceService.calculateQuantityChanges(getUserDto(), Instant.now());
            return true;
                // Тут считаем и добавляем ресурсы с аггрегаций
                // отмечаем аггрегации прочитанными
            // ставим пометку пользователю userDto.setSuperAggregate() == false;
        }
        return resourceService.calculateQuantityChanges(getUserDto(), Instant.now());
    }

    public String getSpoiler() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("HH:mm:ss.SSS");//dd/MM/yyyy
        //return "\n<span class=\"tg-spoiler\">" + sdfDate.format(new Date()) + "</span>";
        return "\n" + sdfDate.format(new Date());
    }

    public String[] getCommandArgsAbsolute(String command) {
        String text = getQuery();
        if (text == null || text.isEmpty()) {
            return null;
        }

        if (!text.startsWith(command)) {
            return null;
        }

        // Убираем команду и разбиваем аргументы
        String argsPart = text.substring(command.length()).trim();
        if (!argsPart.isEmpty()) {
            return argsPart.split("\\s+");
        } else {
            return null;
        }
    }

    public String[] getCommandArgs(String[] commands, String mark) {
        if (commands.length <= 1) return null;
        StringBuilder commandB = new StringBuilder();
        for (int i = 0; i < commands.length; i++) {
            commandB.append(commands[i]).append(" ");
        }
        String command = commandB.toString();
        if (command.isEmpty()) {
            return null;
        }

        if (!command.startsWith(mark)) {
            return null;
        }

        // Убираем команду и разбиваем аргументы
        String argsPart = command.substring(mark.length()).trim();
        if (!argsPart.isEmpty()) {
            return argsPart.split("\\s+");
        } else {
            return null;
        }
    }

    public abstract InlineKeyboardMarkup getInlineKeyboardMarkup();
    public abstract String getText();
    public abstract SendPhoto getSendPhoto();

    public String processReferrerAndReferrals(String code, UserEntity user) {
        boolean isAddReferral = false;
        String message = "";
        //return Map.of(isAddReferral, "Самого себя пригласить не получится! \uD83E\uDD13");
        long referrerId = getReferralService().decodeReferralCode(code);
        //System.out.println("Реферальный код: " + code);
        if (user.getReferrer() != null) {
            //System.out.println("У человека уже есть реферрер");
            return "\uFE0F Вы уже активировали реферальный код ранее! \uD83D\uDE09";
        }
        // Проверка, что пользователь не приглашает сам себя
        //System.out.println("referrerId: " + referrerId);
        //System.out.println("user.getId(): " + (long) user.getId());
        if (referrerId != user.getId()) {
            isAddReferral = getReferralService().processReferral(user, referrerId);
            if (isAddReferral) {
                user.setReferrer(referrerId);
                message = "✅ Реферальный код активирован! \nТеперь Вы будете получать подарки за достижения союзников!";
            }
            message = "Данные сохранить не удалось, попробуйте ещё раз позднее... ";
        } else {
            //System.out.println("Человек приглашает сам себя");
            message = "Самого себя пригласить не получится! \uD83E\uDD13";
        }
        return message;
    }

    private void toGrant() {
        if (!isWithinTimeRange()) {
            return;
        }
        UserDto userDto = getUserDto();
        SchedulerService.grantForUser(userDto);
        userDto.setSuperAggregate(false);
    }

    private boolean isWithinSpecialInterval() {
        LocalTime currentTime = LocalTime.now();
        LocalTime startTime = LocalTime.of(9, 0);  // 9:00
        LocalTime endTime = LocalTime.of(2, 59);   // 2:59

        // Для интервала, пересекающего полночь, нужно особое условие
        if (startTime.isAfter(endTime)) {
            // Интервал пересекает полночь (как в нашем случае)
            return currentTime.isAfter(startTime) || currentTime.isBefore(endTime);
        } else {
            // Обычный интервал в пределах одних суток
            return currentTime.isAfter(startTime) && currentTime.isBefore(endTime);
        }
    }

    // Альтернативная более явная реализация
    private boolean isWithinTimeRange() {
        LocalTime now = LocalTime.now();

        // Если сейчас между 9:00 и 23:59:59.999...
        if (now.isAfter(LocalTime.of(8, 59, 59))) {
            return true;
        }
        // ...или между 00:00 и 02:59
        if (now.isBefore(LocalTime.of(3, 0))) {
            return true;
        }

        // Все остальное время (3:00-8:59) не входит в интервал
        return false;
    }
}
