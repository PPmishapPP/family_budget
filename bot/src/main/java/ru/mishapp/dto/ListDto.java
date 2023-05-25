package ru.mishapp.dto;

import java.util.List;

public record ListDto(List<String> messages, String conclusion) {
    public String toTelegram() {
        return String.join("\n", messages) + "\n-----------------\n" + conclusion;
    }
}
