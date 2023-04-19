package ru.mishapp.services;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.mishapp.dto.Change;
import ru.mishapp.entity.Account;
import ru.mishapp.entity.AccountHistory;
import ru.mishapp.repository.AccountHistoryRepository;
import ru.mishapp.repository.AccountRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChangeService {
    
    private final AccountRepository accountRepository;
    private final AccountHistoryRepository accountHistoryRepository;
    
    
    public int changeBalance(Change change) {
        Optional<Account> byName = accountRepository.findByName(change.name());
        if (byName.isPresent()) {
            AccountHistory lastAccountHistory = accountHistoryRepository.findLast(byName.get().getId());
            AccountHistory newAccountHistory = lastAccountHistory.applyChange(change);
            accountHistoryRepository.save(newAccountHistory);
            return newAccountHistory.getBalance();
        } else {
            throw new IllegalArgumentException("Не существует счёта с именем " + change.name());
        }
    }
}