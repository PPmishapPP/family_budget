package ru.mishapp.services;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mishapp.entity.Account;
import ru.mishapp.entity.AccountHistory;
import ru.mishapp.repository.AccountHistoryRepository;
import ru.mishapp.repository.AccountRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountService {
    
    private final AccountRepository accountRepository;
    private final AccountHistoryRepository accountHistoryRepository;
    
    public Iterable<Account> readAll() {
        return accountRepository.findAll();
    }
    
    public Account readByName(String name, long chatId) {
        Optional<Account> byName = accountRepository.findByNameAndChatId(name, chatId);
        if (byName.isEmpty()) {
            throw new IllegalArgumentException("Нет аккаунта с таким именем");
        }
        return byName.get();
    }
    
    @Transactional
    public Account create(String name, long chatId) {
        Account account = new Account(name, true, chatId);
        Account save = accountRepository.save(account);
        AccountHistory init = accountHistoryRepository.save(
            new AccountHistory(save.getId(), 0, 0, LocalDateTime.now(), "Инициализация"));
        save.getHistory().add(init);
        return save;
    }
}
