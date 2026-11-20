package ru.mishapp.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.mishapp.enumiration.Type;

import java.time.LocalDate;

public record PaymentRuleRequest(
        @NotBlank(message = "Название правила обязательно")
        @Size(max = 256, message = "Название правила не может быть длиннее 256 символов")
        String name,
        @NotNull(message = "Сумма правила обязательна")
        Integer sum,
        @NotNull(message = "Тип правила обязателен")
        Type type,
        @NotNull(message = "Дата следующего платежа обязательна")
        LocalDate nextDay,
        LocalDate endDate
) {
}
