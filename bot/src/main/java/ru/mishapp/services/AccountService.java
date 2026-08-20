package ru.mishapp.services;


import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mishapp.entity.Account;
import ru.mishapp.entity.AccountHistory;
import ru.mishapp.entity.PeriodicChangeRule;
import ru.mishapp.entity.User;
import ru.mishapp.repository.AccountHistoryRepository;
import ru.mishapp.repository.AccountRepository;
import ru.mishapp.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountHistoryRepository accountHistoryRepository;
    private final UserRepository userRepository;

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

    public List<Account> readAllAccounts() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return List.of();
        }
        User user = userRepository.findByLogin(authentication.getName()).orElse(null);
        if (user == null) {
            return List.of();
        }
        return accountRepository.findAllByUserId(user.getId());
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
