package ru.mishapp.dto;

public record AccountBalance(long id, String name, int balance) {
    public String toTelegram() {
        return name + ": " + balance + "₽";
    }
}
