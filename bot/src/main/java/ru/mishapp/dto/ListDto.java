package ru.mishapp.dto;

import java.util.List;

public record ListDto(List<String> messages) {
    public String toTelegram() {
        return String.join("\n", messages);
    }
}
