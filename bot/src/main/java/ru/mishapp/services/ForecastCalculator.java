package ru.mishapp.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.mishapp.dto.PeriodicChangeRuleDto;
import ru.mishapp.entity.Account;
import ru.mishapp.entity.AccountHistory;
import ru.mishapp.entity.PeriodicChangeRule;
import ru.mishapp.mapper.PeriodicChangeRuleMapper;
import ru.mishapp.repository.AccountHistoryRepository;
import ru.mishapp.repository.PeriodicChangeRepository;
import ru.mishapp.services.records.CalcItem;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ForecastCalculator {
    
    private final PeriodicChangeRepository repository;
    private final AccountHistoryRepository accountHistoryRepository;
    private final PeriodicChangeRuleMapper mapper;
    
    public List<CalcItem> calc(Account account, LocalDate to, Long chatId) {
        Map<LocalDate, List<PeriodicChangeRule>> map = repository.findAllByChatId(chatId).stream()
                .flatMap(periodicChange -> periodicChange.getRules().stream())
                .filter(PeriodicChangeRule::isActive)
                .collect(Collectors.groupingBy(PeriodicChangeRule::getNextDay,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> list.stream()
                                        .sorted(Comparator.comparingInt(PeriodicChangeRule::getSum).reversed())
                                        .collect(Collectors.toList())
                        )));
        
        AccountHistory last = accountHistoryRepository.findLast(account.getId());
        int balance = last.getBalance();
        return calc(map, balance, to);
    }

    public List<CalcItem> calc(List<PeriodicChangeRuleDto> dtos, long chatId, LocalDate to) {
        List<PeriodicChangeRule> entityList = mapper.toEntityList(dtos, chatId);
        Map<LocalDate, List<PeriodicChangeRule>> map = entityList.stream()
                .filter(PeriodicChangeRule::isActive)
                .collect(Collectors.groupingBy(PeriodicChangeRule::getNextDay,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> list.stream()
                                        .sorted(Comparator.comparingInt(PeriodicChangeRule::getSum).reversed())
                                        .collect(Collectors.toList())
                        )));
        AccountHistory last = accountHistoryRepository.findLast(entityList.getLast().getTargetAccountId());
        int balance = last.getBalance();
        return calc(map, balance, to);
    }

    private List<CalcItem> calc(Map<LocalDate, List<PeriodicChangeRule>> map, int balance, LocalDate to) {
        List<CalcItem> result = new ArrayList<>();
        for (LocalDate current = LocalDate.now(); !current.isAfter(to); current = current.plusDays(1)) {
            List<PeriodicChangeRule> periodicChangeRules = map.remove(current);
            if (periodicChangeRules != null) {
                for (PeriodicChangeRule rule : periodicChangeRules) {
                    balance = balance + rule.getSum();
                    result.add(new CalcItem(current, balance, rule));
                    LocalDate nextDay = rule.getType().next(rule.getNextDay(), rule.getPass());
                    if (nextDay == null || (rule.getEndDate() != null && nextDay.isAfter(rule.getEndDate()))) {
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
