package ru.mishapp.services;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mishapp.dto.AccountBalance;
import ru.mishapp.entity.Account;
import ru.mishapp.entity.AccountHistory;
import ru.mishapp.entity.PeriodicChangeRule;
import ru.mishapp.repository.AccountHistoryRepository;
import ru.mishapp.repository.AccountRepository;
import ru.mishapp.services.records.ApplyResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class AccountService {
    
    private final AccountRepository accountRepository;
    private final AccountHistoryRepository accountHistoryRepository;
    
    
    public List<AccountBalance> readAllByChatId(Long chatId) {
        return accountRepository.findAllAccountBalanceByChatId(chatId);
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
        accountHistoryRepository.save(
            new AccountHistory(save.getId(), 0, 0, LocalDateTime.now(), "Инициализация"));
        return save;
    }
    
    @Transactional
    public ApplyResult applyRule(PeriodicChangeRule rule) {
        AccountHistory lastTarget = accountHistoryRepository.findLast(rule.getTargetAccountId());
        AccountHistory saveTarget = accountHistoryRepository.save(
            createNext(lastTarget, rule.getSum(), rule.getName())
        );
        Integer targetAccountBalance = saveTarget.getBalance();

        return new ApplyResult(targetAccountBalance, null);
    }

    public List<Account> readAllAccounts() {
        return StreamSupport.stream(accountRepository.findAll().spliterator(), false)
                .toList();
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
