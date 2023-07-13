package ru.mishapp.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.mishapp.entity.Account;
import ru.mishapp.entity.AccountHistory;
import ru.mishapp.entity.PeriodicChangeRule;
import ru.mishapp.repository.AccountHistoryRepository;
import ru.mishapp.repository.PeriodicChangeRepository;
import ru.mishapp.services.records.CalcItem;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ForecastCalculator {
    
    private final PeriodicChangeRepository repository;
    private final AccountHistoryRepository accountHistoryRepository;
    
    public List<CalcItem> calc(Account account, LocalDate to, Long chatId) {
        Map<LocalDate, List<PeriodicChangeRule>> map = repository.findAllByChatId(chatId).stream()
            .flatMap(periodicChange -> periodicChange.getRules().stream())
            .collect(Collectors.groupingBy(PeriodicChangeRule::getNextDay));
        
        AccountHistory last = accountHistoryRepository.findLast(account.getId());
        int balance = last.getBalance();
        
        List<CalcItem> result = new ArrayList<>();
        for (LocalDate current = LocalDate.now(); !current.isAfter(to); current = current.plusDays(1)) {
            List<PeriodicChangeRule> periodicChangeRules = map.remove(current);
            if (periodicChangeRules != null) {
                for (PeriodicChangeRule rule : periodicChangeRules) {
                    balance = balance + rule.getSum();
                    result.add(new CalcItem(current, balance, rule));
                    LocalDate nextDay = rule.getType().next(rule.getNextDay(), rule.getPass());
                    if (nextDay == null) {
                        continue;
                    }
                    PeriodicChangeRule nextRule = rule.withNextDay(nextDay);
                    map.computeIfAbsent(nextDay, day -> new ArrayList<>()).add(nextRule);
                }
            }
        }
        return result;
    }
}
