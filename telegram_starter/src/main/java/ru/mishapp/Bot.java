package ru.mishapp;

import lombok.SneakyThrows;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Optional;

public class Bot extends TelegramLongPollingBot {

    private final HandlerMapping handlerMapping;
    private final String name;


    @SneakyThrows
    public Bot(String botToken, String name, HandlerMapping handlerMapping) {
        super(botToken);
        this.name = name;
        this.handlerMapping = handlerMapping;
    }

    @Override
    @SneakyThrows
    public void onUpdateReceived(Update update) {
        String text = update.getMessage().getText();
        Optional<String> result = handlerMapping.execute(text);
        if (result.isPresent()) {
            execute(new SendMessage(update.getMessage().getChatId().toString(), result.get()));
        }
    }

    @Override
    public String getBotUsername() {
        return name;
    }
}
