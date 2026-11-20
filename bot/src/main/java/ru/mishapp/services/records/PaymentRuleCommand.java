package ru.mishapp.services.records;

import ru.mishapp.enumiration.Type;

import java.time.LocalDate;

public record PaymentRuleCommand(
        String name,
        int sum,
        Type type,
        LocalDate nextDay,
        LocalDate endDate
) {
}
