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
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountHistoryRepository accountHistoryRepository;
    private final CurrentUserService currentUserService;

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

    @Transactional
    public AccountHistory registerPayment(long accountId, String name, int sum) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Название платежа не может быть пустым");
        }
        String comment = name.trim();
        if (comment.length() > 256) {
            throw new IllegalArgumentException("Название платежа не может быть длиннее 256 символов");
        }
        if (sum == 0) {
            throw new IllegalArgumentException("Сумма платежа не может быть равна нулю");
        }

        AccountHistory lastTarget = accountHistoryRepository.findLastByAccountId(accountId);
        int balance = (lastTarget == null ? 0 : lastTarget.getBalance()) + sum;
        AccountHistory payment = new AccountHistory(
                accountId, sum, balance, LocalDateTime.now(), comment);
        return accountHistoryRepository.save(payment);
    }

    public List<Account> readAllAccounts() {
        return currentUserService.getCurrentUser()
                .map(user -> accountRepository.findAllByUserId(user.getId()))
                .orElseGet(List::of);
    }

    public AccountHistory findLastByAccountId(Long accountId) {
        return accountHistoryRepository.findLastByAccountId(accountId);
    }

    public List<AccountHistory> findHistoryByAccountId(Long accountId) {
        return accountHistoryRepository.findHistoryByAccountId(accountId);
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
