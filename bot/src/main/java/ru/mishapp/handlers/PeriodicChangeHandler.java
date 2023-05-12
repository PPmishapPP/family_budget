package ru.mishapp.handlers;

import lombok.RequiredArgsConstructor;
import ru.mishapp.annotations.TelegramCommand;
import ru.mishapp.annotations.TelegramHandler;
import ru.mishapp.annotations.TelegramParam;
import ru.mishapp.entity.PeriodicChange;
import ru.mishapp.services.PeriodicChangeService;

@TelegramHandler("periodic_change")
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class PeriodicChangeHandler {
    
    private final PeriodicChangeService periodicChangeService;
    
    @TelegramCommand("create")
    public String create(@TelegramParam("name") String name, Long chatId) {
        PeriodicChange periodicChange = periodicChangeService.create(name, chatId);
        return  "Создано периодическое изменение - " + periodicChange.toTelegram();
    }
}
