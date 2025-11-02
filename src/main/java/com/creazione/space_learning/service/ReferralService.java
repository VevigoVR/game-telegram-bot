package com.creazione.space_learning.service;

import com.creazione.space_learning.config.IdCodec;
import com.creazione.space_learning.dto.ReferralStats;
import com.creazione.space_learning.entities.game_entity.UserDto;
import com.creazione.space_learning.entities.postgres.ReferralP;
import com.creazione.space_learning.repository.ReferralRepository;
import com.creazione.space_learning.service.postgres.UserPostgresService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

@Service
@Setter
@Getter
public class ReferralService {
    @Value("${bot.name}")
    private String botName;
    private final IdCodec referralCodec;
    private final ReferralRepository referralRepository;
    private final UserPostgresService userService;

    public ReferralService(ReferralRepository referralRepository, UserPostgresService userService) throws NoSuchAlgorithmException, InvalidKeyException {
        this.referralCodec = new IdCodec("1aWEdfHq+LUKebfG53t+1g==");
        this.referralRepository = referralRepository;
        this.userService = userService;
    }

    public ReferralStats getStats(UserDto userDto) {
        return new ReferralStats(userDto.getTotalReferrals());
    }

    public String generateReferralLink(long userId) {
        String code = getCleanReferralCode(userId);
        return "https://t.me/" + botName + "?start=ref_" + code;
    }

    public String getReferralCode(long userId) {
        return getCleanReferralCode(userId);
    }

    private String getCleanReferralCode(long userId) {
        String code = referralCodec.encodeUserId(userId);
        // Удаляем лидирующие нули и первые символы 'A' если они есть
        return removeLeadingCharacters(code, '0', 'A');
    }

    public Long decodeReferralCode(String code) {
        return referralCodec.decodeUserId(code);
    }

    private String removeLeadingCharacters(String input, char... charsToRemove) {
        String toRemove = new String(charsToRemove);
        int index = 0;
        while (index < input.length() && toRemove.indexOf(input.charAt(index)) >= 0) {
            index++;
        }
        return input.substring(index);
    }

    public boolean processReferral(UserDto user, Long referrerId) {
        Long userId = user.getId();
        UserDto referrer = userService.findBasicUserById(referrerId);
        if (referrer != null) {
            referrer.incrementTotalReferrals();
            userService.saveFullWithoutCache(referrer);
        } else {
            return false;
        }
        ReferralP referralEntity = new ReferralP();
        referralEntity.setUserId(userId);
        referralEntity.setReferrerId(referrerId);
        referralEntity.setLastUpdate(new Date());
        referralEntity.setTimeCreate(new Date());
        referralRepository.save(referralEntity);
        user.setReferrer(referrerId);

        return true;
    }
}