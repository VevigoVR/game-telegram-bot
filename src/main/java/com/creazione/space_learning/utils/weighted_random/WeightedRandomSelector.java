package com.creazione.space_learning.utils.weighted_random;

import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Универсальный селектор для взвешенного случайного выбора объектов
 * Можно использовать для карточек, лут-боксов, врагов, событий и т.д.
 */
public class WeightedRandomSelector<T> {

    /**
     * Выбирает случайный объект на основе весов
     * @param items список объектов для выбора
     * @param weightExtractor функция для извлечения веса объекта
     * @return случайно выбранный объект или null если список пуст
     */
    public T selectWeighted(List<T> items, WeightExtractor<T> weightExtractor) {
        if (items == null || items.isEmpty()) {
            return null;
        }

        // Вычисляем общий вес
        double totalWeight = 0.0;
        for (T item : items) {
            totalWeight += weightExtractor.getWeight(item);
        }

        if (totalWeight <= 0) {
            // Если все веса нулевые, возвращаем случайный элемент
            return items.get(ThreadLocalRandom.current().nextInt(items.size()));
        }

        // Взвешенный случайный выбор
        double randomValue = ThreadLocalRandom.current().nextDouble() * totalWeight;
        double currentSum = 0.0;

        for (T item : items) {
            currentSum += weightExtractor.getWeight(item);
            if (currentSum >= randomValue) {
                return item;
            }
        }

        // На всякий случай возвращаем последний элемент
        return items.get(items.size() - 1);
    }

    /**
     * Выбирает несколько уникальных объектов на основе весов
     * @param items список объектов для выбора
     * @param weightExtractor функция для извлечения веса объекта
     * @param count количество объектов для выбора
     * @return список выбранных объектов (может быть меньше count если items недостаточно)
     */
    public List<T> selectWeightedMultiple(List<T> items, WeightExtractor<T> weightExtractor, int count) {
        if (items == null || items.isEmpty() || count <= 0) {
            return Collections.emptyList();
        }

        // Создаем копию списка для работы
        List<T> workingList = new ArrayList<>(items);
        List<T> result = new ArrayList<>();

        for (int i = 0; i < count && !workingList.isEmpty(); i++) {
            T selected = selectWeighted(workingList, weightExtractor);
            if (selected != null) {
                result.add(selected);
                workingList.remove(selected); // Удаляем чтобы не повторяться
            }
        }

        return result;
    }

    /**
     * Интерфейс для извлечения веса объекта
     */
    @FunctionalInterface
    public interface WeightExtractor<T> {
        double getWeight(T item);
    }

    // ==================== РЕАЛИЗАЦИЯ ДЛЯ РАЗНЫХ СЦЕНАРИЕВ ====================

    /**
     * Вспомогательные методы для конкретных сценариев использования
     */
    public static class Selectors {

        // Для карточек обучения (чем больше вес, тем чаще показывается)
        public static <T> WeightedRandomSelector.WeightExtractor<T> cardWeightExtractor() {
            return item -> {
                // Предполагаем, что у карточки есть метод getWeight()
                // В реальности замените на рефлексию или передавайте функцию
                if (item instanceof Weightable) {
                    return ((Weightable) item).getWeight();
                }
                throw new IllegalArgumentException("Item must implement Weightable interface");
            };
        }

        // Для лут-боксов (чем МЕНЬШЕ вес, тем ЧАЩЕ выпадает)
        public static <T> WeightedRandomSelector.WeightExtractor<T> lootWeightExtractor() {
            return item -> {
                if (item instanceof LootItem) {
                    LootItem loot = (LootItem) item;
                    // Инвертируем вес: редкие предметы имеют большой вес, но мы хотим чтобы они выпадали реже
                    return 1.0 / loot.getRarityWeight();
                }
                throw new IllegalArgumentException("Item must implement LootItem interface");
            };
        }

        // Для равномерного распределения (все веса равны)
        public static <T> WeightedRandomSelector.WeightExtractor<T> uniformWeightExtractor() {
            return item -> 1.0;
        }
    }
}

// ==================== ВСПОМОГАТЕЛЬНЫЕ ИНТЕРФЕЙСЫ ====================

/**
 * Интерфейс для объектов с весом (карточки обучения)
 */
interface Weightable {
    double getWeight();
}

/**
 * Интерфейс для предметов лута
 */
interface LootItem {
    double getRarityWeight(); // Чем больше число, тем реже предмет
    String getName();
}

// ==================== ПРИМЕРЫ ИСПОЛЬЗОВАНИЯ ====================

/**
 * Пример карточки для обучения
 */
@Getter
@Setter
class LearningCard implements Weightable {
    private String word;
    private String translation;
    private double weight;

    public LearningCard(String word, String translation, double weight) {
        this.word = word;
        this.translation = translation;
        this.weight = weight;
    }

    @Override
    public double getWeight() {
        return weight;
    }

    // геттеры и сеттеры
}

/**
 * Пример предмета лута
 */
class GameLootItem implements LootItem {
    private String name;
    private double rarityWeight; // 1 - обычный, 5 - редкий, 10 - эпический

    public GameLootItem(String name, double rarityWeight) {
        this.name = name;
        this.rarityWeight = rarityWeight;
    }

    @Override
    public double getRarityWeight() {
        return rarityWeight;
    }

    @Override
    public String getName() {
        return name;
    }
}

/**
 * Демонстрация использования
 */
class WeightedRandomDemo {
    public static void main(String[] args) {
        WeightedRandomSelector<LearningCard> selector = new WeightedRandomSelector<>();

        // Пример 1: Карточки обучения
        List<LearningCard> cards = Arrays.asList(
                new LearningCard("be", "быть", 5.0),  // сложное слово - большой вес
                new LearningCard("go", "идти", 2.0),  // среднее слово
                new LearningCard("to", "к", 1.0)      // простое слово - маленький вес
        );

        LearningCard selectedCard = selector.selectWeighted(
                cards,
                WeightedRandomSelector.Selectors.cardWeightExtractor()
        );
        System.out.println("Выбрана карточка: " + selectedCard.getWord());

        WeightedRandomSelector<GameLootItem> selector2 = new WeightedRandomSelector<>();
        // Пример 2: Лут-бокс
        List<GameLootItem> loot = Arrays.asList(
                new GameLootItem("Золотая монета", 1.0),   // часто
                new GameLootItem("Серебряная монета", 2.0), // реже
                new GameLootItem("Алмаз", 10.0)             // очень редко
        );

        GameLootItem selectedLoot = selector2.selectWeighted(
                loot,
                WeightedRandomSelector.Selectors.lootWeightExtractor()
        );
        System.out.println("Выпал лут: " + selectedLoot.getName());

        // Пример 3: Несколько уникальных карточек
        List<LearningCard> multipleCards = selector.selectWeightedMultiple(
                cards,
                WeightedRandomSelector.Selectors.cardWeightExtractor(),
                2
        );
        System.out.println("Выбрано карточек: " + multipleCards.size());
    }
}

/*
// В FlashCardService
public FlashCard getNextCard(Long userId) {
    List<FlashCard> userCards = getUserCards(userId);
    WeightedRandomSelector<FlashCard> selector = new WeightedRandomSelector<>();

    return selector.selectWeighted(userCards, card -> {
        // card.getWeight() - ваш текущий вес карточки из Redis
        return card.getWeight();
    });
}

// В системе наград
public LootItem openLootBox() {
    List<LootItem> possibleLoot = getLootBoxContents();
    WeightedRandomSelector<LootItem> selector = new WeightedRandomSelector<>();

    return selector.selectWeighted(possibleLoot, item -> {
        // Инвертируем редкость: чем реже предмет, тем меньше вероятность
        return 1.0 / item.getRarity();
    });
}

// В системе случайных событий
public GameEvent getRandomEvent() {
    List<GameEvent> events = getAvailableEvents();
    WeightedRandomSelector<GameEvent> selector = new WeightedRandomSelector<>();

    return selector.selectWeighted(events, event -> event.getProbabilityWeight());
}
*/