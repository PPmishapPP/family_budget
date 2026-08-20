package ru.mishapp.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.mishapp.dto.PeriodicChangeRuleDto;
import ru.mishapp.entity.AccountHistory;
import ru.mishapp.enumiration.Type;
import ru.mishapp.repository.AccountHistoryRepository;
import ru.mishapp.repository.PeriodicChangeRuleRepository;
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

    private final PeriodicChangeRuleRepository periodicChangeRuleRepository;
    private final AccountHistoryRepository accountHistoryRepository;

    public List<CalcItem> calc(Long accountId, LocalDate to) {
        Map<LocalDate, List<PeriodicChangeRuleDto>> map = periodicChangeRuleRepository.findAllRuleDtos(accountId).stream()
                .filter(PeriodicChangeRuleDto::active)
                .collect(Collectors.groupingBy(PeriodicChangeRuleDto::nextDay));

        AccountHistory last = accountHistoryRepository.findLastByAccountId(accountId);
        int balance = last.getBalance();
        return calc(map, balance, to);
    }

    public List<CalcItem> calc(List<PeriodicChangeRuleDto> dtos, LocalDate to) {
        Map<LocalDate, List<PeriodicChangeRuleDto>> map = dtos.stream()
                .filter(PeriodicChangeRuleDto::active)
                .collect(Collectors.groupingBy(PeriodicChangeRuleDto::nextDay));
        AccountHistory last = accountHistoryRepository.findLastByAccountId(dtos.getLast().targetAccountId());
        int balance = last.getBalance();
        return calc(map, balance, to);
    }

    private List<CalcItem> calc(Map<LocalDate, List<PeriodicChangeRuleDto>> map, int balance, LocalDate to) {
        List<CalcItem> result = new ArrayList<>();
        for (LocalDate current = LocalDate.now(); !current.isAfter(to); current = current.plusDays(1)) {
            List<PeriodicChangeRuleDto> periodicChangeRules = map.remove(current);
            if (periodicChangeRules != null) {
                periodicChangeRules.sort(Comparator.comparingInt(PeriodicChangeRuleDto::sum).reversed());
                for (PeriodicChangeRuleDto rule : periodicChangeRules) {
                    balance = balance + rule.sum();
                    result.add(new CalcItem(current, balance, rule));
                    Type type = Type.valueOf(rule.type());
                    LocalDate nextDay = type.next(rule.nextDay(), rule.pass());
                    if (nextDay == null || (rule.endDate() != null && nextDay.isAfter(rule.endDate()))) {
                        continue;
                    }
                    PeriodicChangeRuleDto nextRule = rule.withNextDay(nextDay);
                    map.computeIfAbsent(nextDay, day -> new ArrayList<>()).add(nextRule);
                }
            }
        }
        return result;
    }
}
