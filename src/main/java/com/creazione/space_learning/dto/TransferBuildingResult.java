package com.creazione.space_learning.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Результат перевода ресурсов
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransferBuildingResult {
    private long transferredAmount; // Целая часть для перевода
    private double remainingAmount;  // Дробная часть для сохранения
}
