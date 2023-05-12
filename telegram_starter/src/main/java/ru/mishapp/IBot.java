package ru.mishapp;

public interface IBot {
    
    void sendMessage(String message, long chatId);
    
    void sendError(Throwable e, long chatId);
}
