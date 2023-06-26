package ru.mishapp.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.mishapp.dto.AccountBalance;
import ru.mishapp.dto.ListDto;
import ru.mishapp.entity.Account;
import ru.mishapp.entity.PeriodicChange;
import ru.mishapp.entity.PeriodicChangeRule;
import ru.mishapp.repository.AccountRepository;
import ru.mishapp.repository.PeriodicChangeRepository;
import ru.mishapp.services.records.CalcItem;
import ru.mishapp.services.records.ForecastItem;
import ru.mishapp.services.records.ForecastResult;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.mishapp.Constants.DAY;
import static ru.mishapp.Constants.RUB;

@Service
@RequiredArgsConstructor
public class ForecastService {
    
    private final PeriodicChangeRepository repository;
    private final AccountRepository accountRepository;
    private final ForecastCalculator forecastCalculator;
    
    
    public ForecastResult forecastFor(LocalDate day, Long chatId) {
        List<PeriodicChange> changes = repository.findAllByChatId(chatId);
        List<AccountBalance> accounts = accountRepository.findAllAccountBalanceByChatId(chatId);
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
    
    public ListDto forecastTo(LocalDate to, Account account, Long chatId) {
        List<String> result = forecastCalculator.calc(account, to, chatId)
            .stream()
            .map(calcItem -> String.format(
                "%s: %s₽ (%s %s)",
                calcItem.day().format(DAY),
                RUB.format(calcItem.balance()),
                calcItem.rule().getName(),
                RUB.format(calcItem.rule().getSum()))
            )
            .collect(Collectors.toList());
        
        return new ListDto(result);
    }
    
    public ListDto forecastIncome(LocalDate to, Account account, Long chatId) {
        List<CalcItem> calcItems = forecastCalculator.calc(account, to, chatId);
        
        List<String> result = new ArrayList<>();
        while (true) {
            int min = calcItems.get(0).balance();
            int minIndex = 0;
            for (int i = 1; i < calcItems.size(); i++) {
                CalcItem calcItem = calcItems.get(i);
                if (calcItem.balance() <= min) {
                    min = calcItem.balance();
                    minIndex = i;
                }
            }
            
            if (min > 0 && minIndex > 0) {
                CalcItem income = calcItems.get(0);
                for (int i = minIndex; i >= 0; i--) {
                    CalcItem calcItem = calcItems.get(i);
                    if (calcItem.balance() - min < 0) {
                        income = calcItems.get(i - 1);
                        break;
                    }
                }
                result.add(String.format(
                    "%s: %s₽",
                    income.day().format(DAY),
                    RUB.format(min)
                ));
            }
            
            if (minIndex + 1 == calcItems.size()) {
                return new ListDto(result);
            }
            
            calcItems = calcItems.subList(minIndex + 1, calcItems.size());
        }
        
    }
}
