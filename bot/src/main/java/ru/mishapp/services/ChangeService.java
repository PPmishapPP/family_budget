package ru.mishapp.services;


import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.mishapp.dto.Change;
import ru.mishapp.entity.Account;
import ru.mishapp.entity.AccountHistory;
import ru.mishapp.repository.AccountHistoryRepository;
import ru.mishapp.repository.AccountRepository;

@Service
@RequiredArgsConstructor
public class ChangeService {
    
    private final AccountRepository accountRepository;
    private final AccountHistoryRepository accountHistoryRepository;
    
    
    public int changeBalance(Change change, long chatId) {
        Optional<Account> byName = accountRepository.findByNameAndChatId(change.name(), chatId);
        if (byName.isPresent()) {
            AccountHistory lastAccountHistory = accountHistoryRepository.findLast(byName.get().getId());
            AccountHistory newAccountHistory = lastAccountHistory.applyChange(change);
            accountHistoryRepository.save(newAccountHistory);
            return newAccountHistory.getBalance();
        } else {
            throw new IllegalArgumentException("Не существует счёта с именем " + change.name());
        }
    }
    
    public int update(String name, String comment, int targetBalance, long chatId) {
        Optional<Account> byName = accountRepository.findByNameAndChatId(name, chatId);
        if (byName.isPresent()) {
            AccountHistory lastAccountHistory = accountHistoryRepository.findLast(byName.get().getId());
            int sum = targetBalance - lastAccountHistory.getBalance();
            Change change = new Change(name, sum, comment);
            AccountHistory newAccountHistory = lastAccountHistory.applyChange(change);
            accountHistoryRepository.save(newAccountHistory);
            return newAccountHistory.getBalance();
        } else {
            throw new IllegalArgumentException("Не существует счёта с именем " + name);
        }
    }
}
