package ru.mishapp.handlers;

import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.mishapp.annotations.TelegramCommand;
import ru.mishapp.annotations.TelegramHandler;
import ru.mishapp.annotations.TelegramParam;
import ru.mishapp.dto.AccountBalance;
import ru.mishapp.entity.Account;
import ru.mishapp.services.AccountService;

@TelegramHandler("account")
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("unused")
public class AccountHandler {
    
    private final AccountService accountService;
    
    @TelegramCommand()
    public String readAll(Long chatId) {
        return accountService.readAllByChatId(chatId)
            .stream().map(AccountBalance::toTelegram)
            .collect(Collectors.joining("\n"));
    }
    
    
    @TelegramCommand("read")
    public String readByName(@TelegramParam("name") String name, Long chatId) {
        return accountService.readByName(name, chatId).toTelegram();
    }
    
    @TelegramCommand("create")
    public String create(@TelegramParam("name") String name, Long chatId) {
        Account account = accountService.create(name, chatId);
        return "Создан аккаунт: " + account.toTelegram();
    }
}
