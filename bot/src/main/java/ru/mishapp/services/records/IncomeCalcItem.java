package ru.mishapp.services.records;

import java.time.LocalDate;

public record IncomeCalcItem(LocalDate date, int balance, Integer increase) {
}
