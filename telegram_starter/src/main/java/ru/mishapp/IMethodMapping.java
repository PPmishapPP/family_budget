package ru.mishapp;

public interface IMethodMapping {
    
    String execute(String text, Long chatId);
    
    String toTelegram();
}
