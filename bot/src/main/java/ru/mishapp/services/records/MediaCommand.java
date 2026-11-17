package ru.mishapp.services.records;

import ru.mishapp.enumiration.MediaStatus;
import ru.mishapp.enumiration.MediaType;

public record MediaCommand(
        String name,
        MediaType type,
        MediaStatus status,
        Integer rating,
        String description
) {
}
