package ru.mishapp.api.dto;

import ru.mishapp.enumiration.MediaStatus;
import ru.mishapp.enumiration.MediaType;

public record MediaDto(
        Long id,
        String name,
        MediaType type,
        MediaStatus status,
        Integer rating,
        String description
) {
}
