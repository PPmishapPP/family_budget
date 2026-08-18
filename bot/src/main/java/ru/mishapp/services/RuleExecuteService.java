package ru.mishapp.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.mishapp.Constants;
import ru.mishapp.dto.PeriodicChangeRuleDto;
import ru.mishapp.entity.Account;
import ru.mishapp.entity.PeriodicChange;
import ru.mishapp.entity.PeriodicChangeRule;
import ru.mishapp.mapper.PeriodicChangeRuleMapper;
import ru.mishapp.repository.AccountRepository;
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
    private final PeriodicChangeRuleMapper mapper;
    private final AccountRepository accountRepository;
    private final PeriodicChangeService periodicChangeService;

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

    public String ruleExecute(PeriodicChangeRuleDto dto, long chatId) {
        Account account = accountRepository.findByNameAndChatId("Безопасное место для денег", chatId).orElseThrow(
                () -> new IllegalArgumentException("Не существует счёта с именем 'Безопасное место для денег'")
        );
        Map<String, PeriodicChange> periodicChangeMap = periodicChangeService.findAll(chatId);
        PeriodicChangeRule entity = mapper.toEntity(dto, account, periodicChangeMap);
        ApplyResult applyResult = accountService.applyRule(entity);
        LocalDate nextDay = entity.getType().next(entity.getNextDay(), entity.getPass());

        if (nextDay == null) {
            periodicChangeRuleRepository.save(entity.withActive(false));
        } else if (!nextDay.isEqual(entity.getNextDay())) {
            entity = entity.withNextDay(nextDay);
            if (entity.getEndDate() != null && entity.getEndDate().isBefore(nextDay)) {
                entity = entity.withActive(false);
            }
            periodicChangeRuleRepository.save(entity);
        }
        return "";
    }
}
