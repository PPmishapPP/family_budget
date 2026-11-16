package ru.mishapp.api.dto;

import java.time.LocalDate;

public record ForecastItemDto(
        LocalDate date,
        String operation,
        int sum,
        int balance
) {
}
