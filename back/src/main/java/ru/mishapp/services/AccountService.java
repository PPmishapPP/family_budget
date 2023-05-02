package ru.mishapp.services;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mishapp.config.Constans;
import ru.mishapp.entity.Account;
import ru.mishapp.entity.AccountHistory;
import ru.mishapp.entity.PeriodicChangeRule;
import ru.mishapp.repository.AccountHistoryRepository;
import ru.mishapp.repository.AccountRepository;
import ru.mishapp.services.records.ApplyResult;

import java.time.LocalDate;
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
    
    @Transactional
    public ApplyResult applyRule(PeriodicChangeRule rule, LocalDate day) {
        AccountHistory lastTarget = accountHistoryRepository.findLast(rule.getTargetAccountId());
        AccountHistory saveTarget = accountHistoryRepository.save(
            createNext(lastTarget, rule.getSum(), rule.getName(), day)
        );
        Integer targetAccountBalance = saveTarget.getBalance();
        
        if (rule.getReceivingAccountId() != null) {
            AccountHistory lastReceiving = accountHistoryRepository.findLast(rule.getReceivingAccountId());
            AccountHistory saveReceiving = accountHistoryRepository.save(
                createNext(lastReceiving, rule.getSum() * -1, rule.getName(), day)
            );
            return new ApplyResult(targetAccountBalance, saveReceiving.getBalance());
        } else {
            return new ApplyResult(targetAccountBalance, null);
        }
    }
    
    private AccountHistory createNext(AccountHistory last, int sum, String ruleName, LocalDate ruleDay) {
        return AccountHistory.builder()
            .accountId(last.getAccountId())
            .sum(sum)
            .balance(last.getBalance() + sum)
            .dateTime(LocalDateTime.now())
            .comment(String.format("Применение правила %s за %s", ruleName, ruleDay.format(Constans.DAY)))
            .build();
    }
}
