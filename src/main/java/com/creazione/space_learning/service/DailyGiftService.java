package com.creazione.space_learning.service;

import com.creazione.space_learning.config.DataSet;
import com.creazione.space_learning.dto.MessageText;
import com.creazione.space_learning.dto.UserDto;
import com.creazione.space_learning.entities.DailyGiftEntity;
import com.creazione.space_learning.game.Item;
import com.creazione.space_learning.repository.DailyGiftRepository;
import com.creazione.space_learning.utils.WordUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DailyGiftService {
    private final DailyGiftRepository dailyGiftRepository;
    //private final Random random = new Random();
    //private UserDto userDto = new UserDto();
    private final static int duration = 8;

    public List<Item> takeDailyGift(UserDto userDto, MessageText wrong) {
        Long userId = userDto.getId();
        DailyGiftEntity dailyGiftEntity = dailyGiftRepository.findByUserId(userId);
        if (dailyGiftEntity == null) {
            dailyGiftEntity = new DailyGiftEntity();
            dailyGiftEntity.setUserId(userId);
            dailyGiftEntity.setNumberOfTimes(1);
            dailyGiftEntity.setCalculatedAt(Instant.now());
            dailyGiftRepository.save(dailyGiftEntity);
            return DataSet.getLootBoxService().takeDailyGift(userDto);
        } else {
            CheckResult result = checkHours(dailyGiftEntity.getCalculatedAt());
            if (result.isExpired()) {
                dailyGiftEntity.addNumberOfTimes();
                dailyGiftEntity.setCalculatedAt(Instant.now());
                dailyGiftRepository.save(dailyGiftEntity);
                return DataSet.getLootBoxService().takeDailyGift(userDto);
            } else {
                wrong.setText("С момента последнего подарка прошло менее "
                        + duration + " "
                        + WordUtils.rightWord(duration, "часа", "часов", "часов") + "\n " +
                        "Попробуйте через: " + result.getTimeRemaining());
                return null;
            }
        }
    }

    public static class CheckResult {
        private final boolean expired;
        private final String timeRemaining;

        public CheckResult(boolean expired, String timeRemaining) {
            this.expired = expired;
            this.timeRemaining = timeRemaining;
        }

        // Геттеры
        public boolean isExpired() { return expired; }
        public String getTimeRemaining() { return timeRemaining; }
    }

    public static CheckResult checkHours(Instant lastUpdate) {
        Instant eightHoursLater = lastUpdate.plusSeconds(duration * 3600);
        Instant now = Instant.now();

        if (now.isAfter(eightHoursLater)) {
            return new CheckResult(true, "00:00:00");
        }

        Duration remaining = Duration.between(now, eightHoursLater);
        long totalSeconds = remaining.getSeconds();

        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        String timeRemaining = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        return new CheckResult(false, timeRemaining);
    }

    public boolean isMoreThanHoursPassed(Instant lastUpdate) {
        Instant now = Instant.now();
        Duration duration = Duration.between(lastUpdate, now);
        return duration.toHours() >= DailyGiftService.duration;
    }
}
