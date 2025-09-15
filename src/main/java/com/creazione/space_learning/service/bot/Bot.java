package com.creazione.space_learning.service.bot;

import com.creazione.space_learning.config.DataSet;
import com.creazione.space_learning.entities.UserEntity;
import com.creazione.space_learning.queries.responces.MaintenanceMessage;
import com.creazione.space_learning.queries.responces.Response;
import com.creazione.space_learning.queries.AIPlaceholder;
import com.creazione.space_learning.queries.Query;
import com.creazione.space_learning.queries.QueryList;
import com.creazione.space_learning.service.AIDataCollector;
import com.creazione.space_learning.service.postgres.UserService;
import com.creazione.space_learning.utils.*;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.log4j.Log4j2;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

@Component
@Log4j2
public class Bot extends TelegramLongPollingBot {
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedissonClient redissonClient;
    private final QueryList queryList;
    private final AIPlaceholder aiPlaceholder;
    private final AIDataCollector aiDataCollector;
    private final UserService userService;
    private Long userId = 0L;
    @Value("${bot.name}")
    private String botName;

    private ThrottledMessageSender4 throttledSender4;

    @Autowired
    public Bot(
            @Value("${bot.token}") String botToken, RedisTemplate<String, Object> redisTemplate, RedissonClient redissonClient,
            QueryList queryList,
            AIPlaceholder aiPlaceholder, AIDataCollector aiDataCollector, UserService userService
    ) {
        super(botToken);
        this.redisTemplate = redisTemplate;
        this.redissonClient = redissonClient;
        this.queryList = queryList;
        this.aiPlaceholder = aiPlaceholder;
        this.aiDataCollector = aiDataCollector;
        this.userService = userService;

        DataSet.setThrottledSender4(new ThrottledMessageSender4(
                this,
                redissonClient,
                25 // maxActionsPerSecond
        ));
        throttledSender4 = DataSet.getThrottledSender4();
    }

    @Override
    public void onUpdateReceived(Update update) {
        //System.out.println(update.toString());

        if (update.hasCallbackQuery()) {
            processCallBackQuery(update);
            return;
        }

        if (update.getMessage() == null) {
            return;
        }
        processCommonQuery(update);
    }

    private void processCommonQuery(Update update) {
        String messageText = update.getMessage().getText().trim().toLowerCase();
        this.userId = update.getMessage().getChatId();
        //System.out.println("messageText from " + userId + " : " + messageText);
        boolean isQuery = false;

        // Проверяем не спамит ли пользователь
        if (userId != null) {
            String rateKey = "user_rate:" + userId;
            long requests = redisTemplate.opsForValue().increment(rateKey, 1);
            redisTemplate.expire(rateKey, 1, TimeUnit.MINUTES);

            if (requests > 30) { // > 30 запросов/минуту
                log.warn("User {} is rate-limited", userId);
                return; // Игнорируем сообщение
            }
        }

        for (Query query : queryList.getQueryList()) {
            for (String subQuery : query.getQueries()) {
                String[] text = messageText.split(" ");
                if (text[0].equals(subQuery)) {
                    isQuery = true;
                    if (DataSet.isMaintenance()) {
                        Response response = new MaintenanceMessage(userId);
                        response.initResponse();
                        return;
                    }
                    // Создаём блокировку по userId
                    RLock lock = redissonClient.getLock("userLock:" + userId);

                    try {
                        // Пытаемся заблокировать (ждём до 500мс, TTL=3сек)
                        if (lock.tryLock(500, 3000, TimeUnit.MILLISECONDS)) {
                            throttledSender4.enqueueMessage(query.respond(update), ThrottledMessageSender4.MessagePriority.HIGH);
                        }  else {
                            // Отправляем сообщение о занятости
                            Answer answer = new Answer();
                            answer.setSendMessage(new SendMessage(userId.toString(), "⏳ Система занята, попробуйте через 5 секунд"));
                            throttledSender4.enqueueMessage(answer);
                        }
                    }   catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        // Всегда разблокируем, если блокировка удерживается текущим потоком
                        if (lock.isHeldByCurrentThread()) {
                            lock.unlock();
                        }
                    }
                    break;
                }
            }
        }

        // Если команда не найдена
        if (!isQuery) {
            if (DataSet.isMaintenance()) {
                Response response = new MaintenanceMessage(userId);
                response.initResponse();
                return;
            }
            // Если не команда - логируем для ИИ
            aiDataCollector.logInteraction(
                    userId,
                    messageText,
                    getCurrentGameContext(userId) // контекст игры
            );
            //System.out.println("userId in processCommonQuery(): " + userId);

            // Если не нашли команду - передаем ИИ-заглушке
            /*
            try {
                Answer aiAnswer = aiPlaceholder.respond(update);
                executeAnswer(aiAnswer);
            } catch (TelegramApiException e) {
                // Обработка ошибок
                e.getMessage();
            }

             */
        }
    }

    private void processCallBackQuery(Update update) {
        boolean isQuery = false;
        //System.out.println("Data call back: " + update.getCallbackQuery().getData());
        //System.out.println("messageID: " + update.getCallbackQuery().getMessage().getMessageId());
        String messageText = update.getCallbackQuery().getData().toLowerCase().trim();
        Long userId = update.getCallbackQuery().getMessage().getChatId();
        //System.out.println("messageText from " + userId + " : " + messageText);
        for (Query query : queryList.getQueryList()) {
            //System.out.println("- " + query.getQueries());
            for (String subQuery : query.getQueries()) {
                String[] text = messageText.split(" ");
                if (text[0].equals(subQuery)) {
                    isQuery = true;
                    if (DataSet.isMaintenance()) {
                        Response response = new MaintenanceMessage(userId);
                        response.initResponse();
                        return;
                    }

                    // Создаём блокировку по userId
                    RLock lock = redissonClient.getLock("userLock:" + userId);

                    try {
                        // Пытаемся заблокировать (ждём до 500мс, TTL=3сек)
                        if (lock.tryLock(500, 3000, TimeUnit.MILLISECONDS)) {
                            throttledSender4.enqueueMessage(query.respond(update), ThrottledMessageSender4.MessagePriority.HIGH);
                        }  else {
                            // Отправляем сообщение о занятости
                            Answer answer = new Answer();
                            answer.setSendMessage(new SendMessage(userId.toString(), "⏳ Система занята, попробуйте через 5 секунд"));
                            throttledSender4.enqueueMessage(answer);
                        }
                    }   catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        // Всегда разблокируем, если блокировка удерживается текущим потоком
                        if (lock.isHeldByCurrentThread()) {
                            lock.unlock();
                        }
                    }
                    break;
                }
            }

        }

        // Если команда не найдена
        if (!isQuery) {
            if (DataSet.isMaintenance()) {
                Response response = new MaintenanceMessage(userId);
                response.initResponse();
                return;
            }
            // Если не команда - логируем для ИИ
            aiDataCollector.logInteraction(
                    userId,
                    messageText,
                    getCurrentGameContext(userId) // контекст игры
            );

            //System.out.println("userId in processCallBackQuery(): " + userId);

            // Если не нашли команду - передаем ИИ-заглушке
            /*
            try {
                Answer aiAnswer = aiPlaceholder.respond(update);
                executeAnswer(aiAnswer);
            } catch (TelegramApiException e) {
                // Обработка ошибок
                e.getMessage();
            }

             */
        }
    }

    private void executeAnswer(Answer answer) throws TelegramApiException {
        if (answer.getAnswerCallbackQuery() != null) {
            execute(answer.getAnswerCallbackQuery());
        }

        if (answer.getEditMessageCaption() != null) {
            execute(answer.getEditMessageCaption());
        }

        if (answer.getNewTxt() != null) {
            execute(answer.getNewTxt());
        }

        if (answer.getEditMessageReplyMarkup() != null) {
            execute(answer.getEditMessageReplyMarkup());
        }

        if (answer.getSendMessage() != null) {
            execute(answer.getSendMessage());
        }

        if (answer.getSendPhoto() != null) {
            execute(answer.getSendPhoto());
        }

    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    public String getCurrentGameContext(Long userId) {
        UserEntity user = userService.findFullUserByTelegramId(userId);
        if (user == null) return "new_user";

        return String.format(
                "Ресурсы: %s, Шахты: %s",
                //"Уровень: %d, Ресурсы: %s, Шахты: %d, Карточек: %d",
                //user.getLevel(),
                user.getResources(),
                user.getBuildings()//,
                //user.getLearnedCardsCount()
        );
    }
/*
    @PostConstruct
    public void init() {
        this.throttledSender = new ThrottledMessageSender(this, 25); // 25 msg/sec (запас 20%)
    }
    */
    /*
    @PostConstruct
    // Вместо старой инициализации в @PostConstruct
    public void processDeadLettersOnStartup() {
        throttledSender3.processDeadLetterQueue();
    }

     */

    @PostConstruct
    public void initTest() {
        /*for (int i = 0; i < 1; i++) {
            Answer answer = new Answer();
            answer.setSendMessage(new SendMessage(userId.toString(), "Привет :) Сообщение отправлено из бота, ещё тестирую.\n" +
                    "Взял твой id из своей базы данных. Точно не уверен, но думаю - ты Андрей :)\n" +
                    "Работы с ботом - непочатый край, занимаюсь потихоньку %)))\n\n А у тебя как дела?"));
            System.out.println("Отправлено!");
            throttledSender4.enqueueMessage(answer);
        }

         */
    }

    @PreDestroy
    public void shutdown() {
        throttledSender4.shutdown();
    }
}
