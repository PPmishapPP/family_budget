package ru.mishapp.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record ForecastRequest(
        @NotNull(message = "Дата окончания прогноза обязательна")
        LocalDate to,
        List<@Valid PaymentRuleRequest> rules
) {
}
