package ru.mishapp.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.mishapp.dto.AccountBalance;
import ru.mishapp.entity.PeriodicChange;
import ru.mishapp.entity.PeriodicChangeRule;
import ru.mishapp.repository.AccountRepository;
import ru.mishapp.repository.PeriodicChangeRepository;
import ru.mishapp.services.records.ForecastItem;
import ru.mishapp.services.records.ForecastResult;

@Service
@RequiredArgsConstructor
public class ForecastService {
    
    private final PeriodicChangeRepository repository;
    private final AccountRepository accountRepository;
    public ForecastResult forecastFor(LocalDate day, Long chatId) {
        List<PeriodicChange> changes = repository.findAllByChatId(chatId);
        List<AccountBalance> accounts = accountRepository.findAllNamesByChatId(chatId);
        Map<Long, Integer> accountBalance = accounts.stream()
            .collect(Collectors.toMap(AccountBalance::id, AccountBalance::balance));
        
        List<ForecastItem> rulesForecast = new ArrayList<>();
        for (PeriodicChange periodicChange : changes) {
            int ruleSum = 0;
            for (PeriodicChangeRule rule : periodicChange.getRules()) {
                LocalDate nextDay = rule.getNextDay();
                while (!nextDay.isAfter(day)) {
                    accountBalance.computeIfPresent(rule.getTargetAccountId(), (key, value) -> value + rule.getSum());
                    ruleSum += rule.getSum();
                    nextDay = rule.getType().next(nextDay, rule.getPass());
                }
            }
            rulesForecast.add(new ForecastItem(periodicChange.getName(), ruleSum));
        }
        
        List<ForecastItem> accountsForecast = new ArrayList<>();
        for (AccountBalance account : accounts) {
            accountsForecast.add(new ForecastItem(account.name(), accountBalance.get(account.id())));
        }
        
        return new ForecastResult(rulesForecast, accountsForecast);
    }
}
