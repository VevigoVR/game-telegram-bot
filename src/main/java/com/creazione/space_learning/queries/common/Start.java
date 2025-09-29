package com.creazione.space_learning.queries.common;

import com.creazione.space_learning.entities.game_entity.UserDto;
import com.creazione.space_learning.entities.postgres.PlayerScoreP;
import com.creazione.space_learning.game.resources.Gold;
import com.creazione.space_learning.game.resources.Metal;
import com.creazione.space_learning.game.resources.Stone;
import com.creazione.space_learning.queries.GameCommand;
import com.creazione.space_learning.queries.Query;
import com.creazione.space_learning.utils.Answer;
import com.creazione.space_learning.enums.Emoji;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Component
@GameCommand(
        value = {"/start", "/start@creazionevbot", "старт", "начать", ".старт", "start"},
        description = "Начало игры"
)
public class Start extends Query {

    public Start() {
        super(new ArrayList<>());
    }

    @Override
    public Answer respond(Update update) {
        Answer answer = new Answer();
        setChatId(update.getMessage().getChatId());
        String userName = craftUserName(update.getMessage().getFrom());
        UserDto userDto = userService.findFullUserByTelegramId(getChatId());
        if (userDto == null) {
            UserDto user = new UserDto();
            user.setTelegramId(getChatId());
            setQuery(update.getMessage().getText().trim());
            user.setName(userName);
            user = userService.saveFullWithoutCache(user);

            // ПРОВЕРКА, ЕСТЬ ЛИ РЕФЕРАЛЬНЫЙ КОД ПРИГЛАШЕНИЯ
            String[] args = getQuery().split(" ");
            if (args.length > 1 && args[1].startsWith("ref_")) {
                String code = args[1].substring(4);
                processReferrerAndReferrals(code, user);
            }

            user.setPlayerScore(new PlayerScoreP(user.getId()));
            Gold gold = new Gold(25);
            gold.setUserId(user.getId());
            /*
            Metal metal = new Metal(25000);
            metal.setUserId(user.getId());
            Stone stone = new Stone(25000);
            stone.setUserId(user.getId());

             */

/*
            MetalBuilding metalBuilding = new MetalBuilding();
            metalBuilding.setUserId(user.getId());
            metalBuilding.setLevel(35);
 */

/*
            InventoryBooster inventoryBooster = new InventoryBooster(
                    ResourceType.ACCELERATION_ALL,
                    1.5, Duration.ofHours(24).toMillis(), 1000);
            inventoryBooster.setUserId(user.getId());
 */

            user.setResources(List.of(gold));
//            user.setBoosters(Set.of(inventoryBooster));
//            user.setBuildings(Set.of(metalBuilding));
            userService.saveFull(user);

            String img = "/static/image/start.jpg";
            String targetImg = "start.jpg";
            String text = getWelcomeText(userName);
            InlineKeyboardMarkup markupInline = getInlineKeyboardMarkup();
            SendPhoto message = sendCustomPhoto(getChatId(), img, targetImg, text);
            message.setReplyMarkup(markupInline);
            answer.setSendPhoto(message);
            return answer;
        } else {
            setUserDto(userDto);
            Profile profile = new Profile();
            return profile.respondWithoutUser(update, getUserDto());
        }
    }

    private String craftUserName(User user) {
        // Собираем имя из частей
        String firstName = user.getFirstName() != null ? user.getFirstName() : "";
        String lastName = user.getLastName() != null ? " " + user.getLastName() : "";

        // Если есть username - используем его
        if (user.getUserName() != null && !user.getUserName().isEmpty()) {
            return "@" + user.getUserName();
        }

        // Если есть имя - используем его
        if (!firstName.isEmpty()) {
            return firstName + lastName;
        }

        // Если вообще нет информации - используем ID
        return "Astronaut #" + user.getId();
    }

    private String getWelcomeText(String userName) {
        return Emoji.STAR2 +
                "<b>"
                + userName +
                """
                </b>, добро пожаловать во вселенную <b>Creazione</b>"""
                + Emoji.STAR2 +
                """
                
                
                """
                + Emoji.MAN_ASTRONAUT + """
                После упорных поисков, Вы нашли пригодную для жизни планету и начали её освоение!
                """;
    }

    @Override
    public InlineKeyboardMarkup getInlineKeyboardMarkup() {
        List<Integer> buttonsInLine = List.of(1);
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        buttons.add(getButton("Обзор планеты", "/profileNewWindow"));
        return getKeyboard(buttonsInLine, buttons);
    }

    @Override
    public String getText() {
        return "";
    }

    @Override
    public SendPhoto getSendPhoto() {
        return null;
    }
}
