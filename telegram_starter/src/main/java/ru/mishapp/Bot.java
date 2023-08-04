package ru.mishapp;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import lombok.SneakyThrows;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public class Bot extends TelegramLongPollingBot implements IBot {
    
    private final HandlerMapping handlerMapping;
    private final String name;
    
    
    @SneakyThrows
    public Bot(String botToken, String name, HandlerMapping handlerMapping) {
        super(botToken);
        this.name = name;
        this.handlerMapping = handlerMapping;
    }
    
    @Override
    @SuppressWarnings("java:S1181")
    public void onUpdateReceived(Update update) {
        String text = update.getMessage().getText();
        try {
            Optional<String> result = handlerMapping.execute(text, update.getMessage().getChatId());
            if (result.isPresent()) {
                execute(new SendMessage(update.getMessage().getChatId().toString(), result.get()));
            }
        } catch (Throwable e) {
            sendError(e, update.getMessage().getChatId());
        }
    }
    
    @SneakyThrows
    @Override
    public void sendMessage(String message, long chatId) {
        execute(new SendMessage(String.valueOf(chatId), message));
    }
    
    @SneakyThrows
    @Override
    public void sendError(Throwable e, long chatId) {
        if (e instanceof InvocationTargetException targetException) {
            e = targetException.getTargetException();
        }
        
        StringBuilder builder = new StringBuilder();
        if (e.getMessage() != null) {
            builder.append(e.getMessage());
        }
        for (Throwable throwable : e.getSuppressed()) {
            if (throwable.getMessage() != null) {
                builder.append(throwable.getMessage());
            }
        }
        
        execute(new SendMessage(String.valueOf(chatId), builder.toString()));
        e.printStackTrace();
    }
    
    @Override
    public String getBotUsername() {
        return name;
    }
    
}
