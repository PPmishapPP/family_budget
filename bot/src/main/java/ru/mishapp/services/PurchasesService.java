package ru.mishapp.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.mishapp.Constants;
import ru.mishapp.dto.ListDto;
import ru.mishapp.entity.PeriodicChangeRule;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static ru.mishapp.Constants.DAY;

@RequiredArgsConstructor
@Service
public class PurchasesService {
    
    private final PeriodicChangeRuleService ruleService;
    
    public ListDto readAll(Long chatId) {
        Set<PeriodicChangeRule> rules = ruleService.readAllByName("Покупки", chatId);
        
        List<String> messages = rules.stream()
            .sorted(Comparator.comparing(PeriodicChangeRule::getNextDay, Comparator.naturalOrder()))
            .map(rule -> String.format("%s %s: %s₽",
                rule.getNextDay().format(DAY),
                rule.getName(),
                Constants.RUB.format(rule.getSum()))
            ).toList();
        
        return new ListDto(messages);
    }
}
