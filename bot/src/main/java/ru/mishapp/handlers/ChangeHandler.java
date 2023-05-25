package ru.mishapp.handlers;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import ru.mishapp.Constants;
import ru.mishapp.annotations.TelegramCommand;
import ru.mishapp.annotations.TelegramHandler;
import ru.mishapp.annotations.TelegramParam;
import ru.mishapp.dto.Change;
import ru.mishapp.services.ChangeService;

@TelegramHandler(value = "вручную", description = "Внести какое-то изменение в счета")
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class ChangeHandler {
    
    private final ChangeService changeService;
    
    @TelegramCommand("пополнить")
    @SneakyThrows
    public String add(
        @TelegramParam("счёт") String accountName,
        @TelegramParam("сумма") String sum,
        @TelegramParam("комментарий") String comment,
        Long chatId
    ) {
        Change change = new Change(accountName, Integer.parseInt(sum), comment);
        int balance = changeService.changeBalance(change, chatId);
        return String.format("Баланс у счёта %s изменён. Текущий баланс: %s₽", change.name(), Constants.RUB.format(balance));
    }
    
    @TelegramCommand("снять")
    @SneakyThrows
    public String delete(
        @TelegramParam("счёт") String accountName,
        @TelegramParam("сумма") String sum,
        @TelegramParam("комментарий") String comment,
        Long chatId
    ) {
        Change change = new Change(accountName, -1 * Integer.parseInt(sum), comment);
        int balance = changeService.changeBalance(change, chatId);
        return String.format("Баланс у счёта %s изменён. Текущий баланс: %d₽", change.name(), balance);
    }
}
