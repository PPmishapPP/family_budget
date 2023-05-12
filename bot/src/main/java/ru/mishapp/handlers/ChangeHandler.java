package ru.mishapp.handlers;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import ru.mishapp.annotations.TelegramCommand;
import ru.mishapp.annotations.TelegramHandler;
import ru.mishapp.annotations.TelegramParam;
import ru.mishapp.dto.Change;
import ru.mishapp.services.ChangeService;

@TelegramHandler("change")
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class ChangeHandler {
    
    private final ChangeService changeService;
    
    @TelegramCommand("add")
    @SneakyThrows
    public String add(
        @TelegramParam("name") String accountName,
        @TelegramParam("sum") String sum,
        @TelegramParam("comment") String comment,
        Long chatId
    ) {
        Change change = new Change(accountName, Integer.parseInt(sum), comment);
        int balance = changeService.changeBalance(change, chatId);
        return String.format("Баланс у счёта %s изменён. Текущий баланс: %d₽", change.name(), balance);
    }
    
    @TelegramCommand("delete")
    @SneakyThrows
    public String delete(
        @TelegramParam("name") String accountName,
        @TelegramParam("sum") String sum,
        @TelegramParam("comment") String comment,
        Long chatId
    ) {
        Change change = new Change(accountName, -1 * Integer.parseInt(sum), comment);
        int balance = changeService.changeBalance(change, chatId);
        return String.format("Баланс у счёта %s изменён. Текущий баланс: %d₽", change.name(), balance);
    }
}
