package ru.mishapp.services;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mishapp.entity.Account;
import ru.mishapp.entity.AccountHistory;
import ru.mishapp.entity.PeriodicChangeRule;
import ru.mishapp.repository.AccountHistoryRepository;
import ru.mishapp.repository.AccountRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountHistoryRepository accountHistoryRepository;

    @Transactional
    public Account create(String name) {
        Account account = new Account(name, true);
        Account save = accountRepository.save(account);
        accountHistoryRepository.save(
                new AccountHistory(save.getId(), 0, 0, LocalDateTime.now(), "Инициализация"));
        return save;
    }

    @Transactional
    public void applyRule(PeriodicChangeRule rule) {
        AccountHistory lastTarget = accountHistoryRepository.findLastByAccountId(rule.getTargetAccountId());
        accountHistoryRepository.save(
                createNext(lastTarget, rule.getSum(), rule.getName())
        );
    }

    public Iterable<Account> readAllAccounts() {
        return accountRepository.findAll();
    }

    public AccountHistory findLastByAccountId(Long accountId) {
        return accountHistoryRepository.findLastByAccountId(accountId);
    }

    private AccountHistory createNext(AccountHistory last, int sum, String ruleName) {
        return AccountHistory.builder()
                .accountId(last.getAccountId())
                .sum(sum)
                .balance(last.getBalance() + sum)
                .dateTime(LocalDateTime.now())
                .comment(ruleName)
                .build();
    }
}
