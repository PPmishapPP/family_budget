package ru.mishapp.api.dto;

import java.time.LocalDateTime;

public record AccountHistoryDto(
        Long id,
        int sum,
        int balance,
        LocalDateTime dateTime,
        String comment
) {
}
