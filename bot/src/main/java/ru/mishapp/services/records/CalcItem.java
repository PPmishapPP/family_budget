package ru.mishapp.services.records;

import ru.mishapp.dto.PeriodicChangeRuleDto;

import java.time.LocalDate;

public record CalcItem(LocalDate day, int balance, PeriodicChangeRuleDto rule) {
}
