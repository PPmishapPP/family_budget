package ru.mishapp.api.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record IncomeForecastRequest(
        @NotNull(message = "Дата окончания прогноза обязательна")
        LocalDate to
) {
}
