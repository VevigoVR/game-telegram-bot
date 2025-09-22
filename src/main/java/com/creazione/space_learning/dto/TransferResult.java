package com.creazione.space_learning.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

// Результат перевода ресурсов
@Getter
@AllArgsConstructor
public class TransferResult {
    private long transferredAmount; // Целая часть для перевода
    private double remainingAmount;  // Дробная часть для сохранения
}
