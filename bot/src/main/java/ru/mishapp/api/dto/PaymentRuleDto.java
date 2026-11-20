package ru.mishapp.api.dto;

import ru.mishapp.enumiration.Type;

import java.time.LocalDate;

public record PaymentRuleDto(
        Long id,
        String name,
        int sum,
        Type type,
        int pass,
        LocalDate nextDay,
        LocalDate endDate
) {
}
