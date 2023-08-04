package ru.mishapp.services.records;

import ru.mishapp.entity.PeriodicChangeRule;

import java.time.LocalDate;

public record CalcItem(LocalDate day, int balance, PeriodicChangeRule rule) {
}
