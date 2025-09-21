package com.creazione.space_learning.dto;

import com.creazione.space_learning.entities.postgres.ResourceP;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// Результат перевода ресурсов
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TransferResult {
    private ResourceP buyResource;
    private ResourceP sellResource;
    private long transferredAmount; // Целая часть для перевода
    private double remainingAmount;  // Дробная часть для сохранения
    private String message;

    public TransferResult (String message) {
        this.message = message;
    }
}
