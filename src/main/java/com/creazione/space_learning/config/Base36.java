package com.creazione.space_learning.config;

import java.math.BigInteger;

public class Base36 {
    private static final String DIGITS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final BigInteger BASE = BigInteger.valueOf(36);

    // Кодирование без дополнения нулями
    public static String encode(byte[] data) {
        BigInteger number = new BigInteger(1, data);
        if (number.equals(BigInteger.ZERO)) {
            return "0";
        }

        StringBuilder result = new StringBuilder();
        while (number.compareTo(BigInteger.ZERO) > 0) {
            BigInteger[] quotientAndRemainder = number.divideAndRemainder(BASE);
            number = quotientAndRemainder[0];
            int digit = quotientAndRemainder[1].intValue();
            result.insert(0, DIGITS.charAt(digit));
        }
        return result.toString();
    }

    // Декодирование с обработкой переменной длины
    public static byte[] decode(String input) {
        BigInteger number = BigInteger.ZERO;
        String cleanInput = input.trim().toUpperCase();

        for (char c : cleanInput.toCharArray()) {
            int digit = DIGITS.indexOf(c);
            if (digit == -1) {
                throw new IllegalArgumentException("Invalid character: " + c);
            }
            number = number.multiply(BASE).add(BigInteger.valueOf(digit));
        }

        return number.toByteArray();
    }
}