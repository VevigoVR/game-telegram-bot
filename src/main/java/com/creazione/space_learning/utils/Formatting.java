package com.creazione.space_learning.utils;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

@Component
public class Formatting {

    private static final NumberFormat GERMAN_NUMBER_FORMAT =
            NumberFormat.getInstance(Locale.GERMAN);

    public static String formatWithDots(long number) {
        return GERMAN_NUMBER_FORMAT.format(number);
    }

    public static String formatWithDots(double number) {
        number = Math.floor(number);
        return GERMAN_NUMBER_FORMAT.format(number);
    }

    // Метод для форматирования без дробной части (округляет в сторону нуля)
    public static String formatWithoutFraction(double number) {
        if (Double.isInfinite(number) || Double.isNaN(number)) {
            return String.valueOf(number);
        }
        BigDecimal bd = new BigDecimal(String.valueOf(number));
        bd = bd.setScale(0, RoundingMode.DOWN);
        return bd.toPlainString();
    }

    // Метод для форматирования с дробной частью (до двух знаков, без trailing нулей, округляет в сторону нуля)
    public static String formatWithFraction(double number) {
        if (Double.isInfinite(number) || Double.isNaN(number)) {
            return String.valueOf(number);
        }
        BigDecimal bd = new BigDecimal(String.valueOf(number));
        bd = bd.setScale(2, RoundingMode.DOWN);
        // Убираем trailing нули и точку, если нужно
        return bd.stripTrailingZeros().toPlainString();
    }
}
