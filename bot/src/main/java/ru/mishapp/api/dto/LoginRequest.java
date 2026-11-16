package ru.mishapp.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Логин обязателен")
        @Size(max = 100, message = "Логин не может быть длиннее 100 символов")
        String login,
        @NotBlank(message = "Пароль обязателен")
        @Size(max = 200, message = "Пароль не может быть длиннее 200 символов")
        String password
) {
}
