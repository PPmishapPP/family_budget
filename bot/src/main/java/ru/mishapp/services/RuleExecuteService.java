package ru.mishapp.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.mishapp.Constants;
import ru.mishapp.entity.PeriodicChange;
import ru.mishapp.entity.PeriodicChangeRule;
import ru.mishapp.repository.PeriodicChangeRepository;
import ru.mishapp.repository.PeriodicChangeRuleRepository;
import ru.mishapp.services.records.ApplyResult;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class RuleExecuteService {
    
    private final PeriodicChangeRuleRepository periodicChangeRuleRepository;
    private final PeriodicChangeRepository periodicChangeRepository;
    private final AccountService accountService;

    public Map<Long, List<String>> ruleExecute(LocalDate day) {
        Map<Long, List<String>> messages = new HashMap<>();
        Map<Long, Long> chatIdByPeriodChangeId = StreamSupport
            .stream(periodicChangeRepository.findAll().spliterator(), false)
            .collect(Collectors.toMap(PeriodicChange::getId, PeriodicChange::getChatId, (c1, c2) -> c1));

        List<PeriodicChangeRule> periodicChangeRules = periodicChangeRuleRepository.findByActiveTrueAndNextDayLessThanEqual(day);
        for (PeriodicChangeRule rule : periodicChangeRules) {
            LocalDate nextDay = rule.getNextDay();
            Long chatId = chatIdByPeriodChangeId.get(rule.getPeriodicChangeId());
            
            while (!nextDay.isAfter(day)) {
                ApplyResult applyResult = accountService.applyRule(rule);
                String message = String.format(
                    "%s = %s₽",
                    rule.toTelegram(), Constants.RUB.format(applyResult.targetAccountBalance())
                );
                messages.computeIfAbsent(chatId, id -> new ArrayList<>()).add(message);
                nextDay = rule.getType().next(nextDay, rule.getPass());

                if (nextDay == null || (rule.getEndDate() != null && rule.getEndDate().isBefore(nextDay))) {
                    break;
                }
            }

            if (nextDay == null) {
                periodicChangeRuleRepository.save(rule.withActive(false));
            } else if (!nextDay.isEqual(rule.getNextDay())) {
                rule = rule.withNextDay(nextDay);
                if (rule.getEndDate() != null && rule.getEndDate().isBefore(nextDay)) {
                    rule = rule.withActive(false);
                }
                periodicChangeRuleRepository.save(rule);
            }
        }
        
        return messages;
    }
}
