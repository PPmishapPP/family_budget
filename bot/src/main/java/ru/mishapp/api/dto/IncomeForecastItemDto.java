package ru.mishapp.api.dto;

import java.time.LocalDate;

public record IncomeForecastItemDto(
        LocalDate date,
        int balance,
        Integer increase
) {
}
