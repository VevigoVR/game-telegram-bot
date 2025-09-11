package com.creazione.space_learning.utils;

public class WordUtils {

    /**
     * Оптимизированный метод склонения слов
     *
     * @param n  Число
     * @param f1 Форма для 1 (яблоко)
     * @param f2 Форма для 2-4 (яблока)
     * @param f5 Форма для 5-0 (яблок)
     * @return Правильная форма слова
     */
    @Deprecated
    public static String rightWord2(int n, String f1, String f2, String f5) {
        int mod100 = Math.abs(n) % 100;
        int mod10 = mod100 % 10;

        // Обработка исключений 11-14
        if (mod100 >= 11 && mod100 <= 14) {
            return f5;
        }

        // Основная логика выбора формы
        return (mod10 == 1) ? f1 :
                (mod10 >= 2 && mod10 <= 4) ? f2 : f5;
    }

    public static String rightWord(int n, String f1, String f2, String f5) {
        int mod = Math.abs(n) % 100;
        return (mod > 10 && mod < 15) ? f5 :
                (mod % 10 == 1) ? f1 :
                        (mod % 10 > 1 && mod % 10 < 5) ? f2 : f5;
    }

    // Тесты
    public static void main(String[] args) {
        int[] testValues = {0, 1, 2, 5, 11, 21, 22, 25, 111, 212};
        for (int n : testValues) {
            System.out.printf("%d %s%n", n, rightWord(n, "яблоко", "яблока", "яблок"));
        }
        for (int n : testValues) {
            System.out.printf("%d %s%n", n, rightWord2(n, "яблоко", "яблока", "яблок"));
        }
    }
}
