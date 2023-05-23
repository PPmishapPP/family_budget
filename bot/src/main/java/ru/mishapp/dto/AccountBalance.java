package ru.mishapp.dto;

import ru.mishapp.Constants;

public record AccountBalance(long id, String name, int balance) {
    public String toTelegram() {
        return name + ": " + Constants.RUB.format(balance) + "₽";
    }
}
