package ru.mishapp.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.mishapp.enumiration.MediaStatus;
import ru.mishapp.enumiration.MediaType;

public record MediaRequest(
        @NotBlank(message = "Название обязательно")
        @Size(max = 256, message = "Название не может быть длиннее 256 символов")
        String name,
        @NotNull(message = "Тип обязателен")
        MediaType type,
        MediaStatus status,
        @Min(value = 1, message = "Рейтинг должен быть от 1 до 10")
        @Max(value = 10, message = "Рейтинг должен быть от 1 до 10")
        Integer rating,
        @Size(max = 2000, message = "Комментарий не может быть длиннее 2000 символов")
        String description
) {
}
