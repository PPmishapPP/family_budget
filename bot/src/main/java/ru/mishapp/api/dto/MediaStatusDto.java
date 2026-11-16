package ru.mishapp.api.dto;

import java.util.List;

public record MediaStatusDto(
        String value,
        String label,
        List<String> availableTransitions
) {
}
