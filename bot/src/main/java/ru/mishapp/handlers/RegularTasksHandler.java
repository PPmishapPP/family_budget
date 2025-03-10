package ru.mishapp.handlers;

import lombok.RequiredArgsConstructor;
import ru.mishapp.annotations.TelegramCommand;
import ru.mishapp.annotations.TelegramHandler;
import ru.mishapp.annotations.TelegramParam;
import ru.mishapp.services.RegularTasksService;

@TelegramHandler(value = "задача", description = "Регулярные задачи")
@RequiredArgsConstructor
public class RegularTasksHandler {

    private final RegularTasksService regularTasksService;

    @TelegramCommand("выполнена")
    public String done(@TelegramParam("тип") String type, Long chatId) {
        return regularTasksService.done(type, chatId);
    }

    @TelegramCommand("отложить")
    public String putOff(@TelegramParam("на") String hours, @TelegramParam("тип") String type, Long chatId) {
        return regularTasksService.putOff(type, hours, chatId);
    }
}