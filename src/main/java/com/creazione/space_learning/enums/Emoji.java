package com.creazione.space_learning.enums;

import com.vdurmont.emoji.EmojiParser;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Emoji {
    STAR(EmojiParser.parseToUnicode(":star:")),
    STAR2(EmojiParser.parseToUnicode(":star2:")),
    DIZZY(EmojiParser.parseToUnicode(":dizzy:")),
    SPARKLES(EmojiParser.parseToUnicode(":sparkles:")),
    SPARKLE(EmojiParser.parseToUnicode(":sparkle:")),
    MAN_ASTRONAUT(EmojiParser.parseToUnicode(":man_astronaut:")),
    ROCK("\uD83E\uDEA8"), // STONE
    WOOD("\uD83E\uDEB5"), // WOOD
    FULL_MOON(EmojiParser.parseToUnicode(":full_moon:")), // GOLD
    FLYING_DISC("\uD83E\uDD4F"), // CRYPTO
    NAZAR_AMULET("\uD83E\uDDFF"), // COIN
    COIN("\uD83E\uDE99"),
    MONEY(EmojiParser.parseToUnicode(":moneybag:")), // CRYPTO
    BLACK_CIRCLE(EmojiParser.parseToUnicode(":black_circle:")),
    WHITE_SMALL_SQUARE(EmojiParser.parseToUnicode(":white_small_square:")),
    UNKNOWN(""),
    ARROWS_COUNTERCLOCKWISE(EmojiParser.parseToUnicode(":arrows_counterclockwise:")),
    ARROW_DOWN(EmojiParser.parseToUnicode(":arrow_down:")),
    ARROW_LEFT(EmojiParser.parseToUnicode(":arrow_left:")),
    ARROW_UP(EmojiParser.parseToUnicode(":arrow_up:")),
    ARROW_RIGHT(EmojiParser.parseToUnicode(":arrow_right:")),
    HOUSE(EmojiParser.parseToUnicode(":house:")),
    EXCLAMATION(EmojiParser.parseToUnicode(":exclamation:")),
    BUSTS_IN_SILHOUETTE(EmojiParser.parseToUnicode(":busts_in_silhouette:")),
    EJECT_SYMBOL("\u23CF"), // ⏏
    SCHOOL_SATCHEL(EmojiParser.parseToUnicode(":school_satchel:")),
    REFERRAL_BOX_2(EmojiParser.parseToUnicode("\uD83E\uDDF0")),
    REFERRAL_BOX_3(EmojiParser.parseToUnicode("\uD83C\uDF81")),
    DEPARTMENT_STORE(EmojiParser.parseToUnicode("🏬")),
    STELLITE(EmojiParser.parseToUnicode("\uD83D\uDCE1")),
    POST_WITH_MAILS(EmojiParser.parseToUnicode("\uD83D\uDCEC")),
    POST_WITHOUT_MAILS(EmojiParser.parseToUnicode("\uD83D\uDCEA")),
    POST_BOX(EmojiParser.parseToUnicode("\uD83D\uDCEE")),
    TEXT(EmojiParser.parseToUnicode("\uD83D\uDCDD")),
    OUTBOX_TRAY(EmojiParser.parseToUnicode("\uD83D\uDCE4")),
    GEAR(EmojiParser.parseToUnicode(":gear:")),
    PACKAGE(EmojiParser.parseToUnicode("\uD83D\uDCE6")),
    CARD_FILE_BOX(EmojiParser.parseToUnicode("\uD83D\uDDC3"))
    ;
    private final String name;

    @Override
    public String toString() {
        return name;
    }
}
