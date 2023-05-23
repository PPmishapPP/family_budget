package ru.mishapp.handlers;

import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import ru.mishapp.annotations.TelegramCommand;
import ru.mishapp.annotations.TelegramHandler;
import ru.mishapp.annotations.TelegramParam;
import ru.mishapp.entity.PeriodicChange;
import ru.mishapp.services.PeriodicChangeService;

@TelegramHandler(value = "изменения", description = "Добавить группу расходов/доходов")
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class PeriodicChangeHandler {
    
    private final PeriodicChangeService periodicChangeService;
    
    @TelegramCommand()
    public String readAll(Long chatId) {
        return periodicChangeService.readAll(chatId)
            .stream()
            .map(PeriodicChange::toTelegram)
            .collect(Collectors.joining("\n"));
    }
    
    @TelegramCommand("добавить")
    public String create(@TelegramParam("изменение") String name, Long chatId) {
        PeriodicChange periodicChange = periodicChangeService.create(name, chatId);
        return  "Создано периодическое изменение - " + periodicChange.toTelegram();
    }
}
