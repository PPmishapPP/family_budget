package ru.mishapp.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.mishapp.enumiration.PaymentDirection;

public record InstantPaymentRequest(
        @NotBlank(message = "Название платежа обязательно")
        @Size(max = 256, message = "Название платежа не может быть длиннее 256 символов")
        String name,
        @NotNull(message = "Сумма платежа обязательна")
        @Min(value = 1, message = "Сумма платежа должна быть больше нуля")
        Integer amount,
        @NotNull(message = "Тип операции обязателен")
        PaymentDirection direction
) {
}
