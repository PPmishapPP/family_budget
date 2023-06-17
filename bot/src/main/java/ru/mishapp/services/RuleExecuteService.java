package ru.mishapp.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.mishapp.Constants;
import ru.mishapp.dto.AccountNames;
import ru.mishapp.entity.PeriodicChange;
import ru.mishapp.entity.PeriodicChangeRule;
import ru.mishapp.repository.AccountRepository;
import ru.mishapp.repository.PeriodicChangeRepository;
import ru.mishapp.repository.PeriodicChangeRuleRepository;
import ru.mishapp.services.records.ApplyResult;

@Service
@RequiredArgsConstructor
public class RuleExecuteService {
    
    private final PeriodicChangeRuleRepository periodicChangeRuleRepository;
    private final PeriodicChangeRepository periodicChangeRepository;
    private final AccountRepository accountRepository;
    private final AccountService accountService;
    
    public Map<Long, List<String>> ruleExecute(LocalDate day) {
        Map<Long, List<String>> messages = new HashMap<>();
        Map<Long, Long> chatIdByPeriodChangeId = StreamSupport
            .stream(periodicChangeRepository.findAll().spliterator(), false)
            .collect(Collectors.toMap(PeriodicChange::getId, PeriodicChange::getChatId, (c1, c2) -> c1));
        Map<Long, String> accounts = accountRepository.findAllNames().stream()
            .collect(Collectors.toMap(AccountNames::id, AccountNames::name));
        
        for (PeriodicChangeRule rule : periodicChangeRuleRepository.findAll()) {
            LocalDate nextDay = rule.getNextDay();
            Long chatId = chatIdByPeriodChangeId.get(rule.getPeriodicChangeId());
            
            while (!nextDay.isAfter(day)) {
                ApplyResult applyResult = accountService.applyRule(rule, nextDay);
                nextDay = rule.getType().next(nextDay, rule.getPass());
                String message = String.format(
                    "Применено правило - %s%nТекущий баланс на счету %s: %s",
                    rule.toTelegram(), accounts.get(rule.getTargetAccountId()), Constants.RUB.format(applyResult.TargetAccountBalance())
                );
                messages.computeIfAbsent(chatId, id -> new ArrayList<>()).add(message);
            }
            
            if (!nextDay.isEqual(rule.getNextDay())) {
                periodicChangeRuleRepository.save(rule.withNextDay(nextDay));
            }
        }
        
        return messages;
    }
}
