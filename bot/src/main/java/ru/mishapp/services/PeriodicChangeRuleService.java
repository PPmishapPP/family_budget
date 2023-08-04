package ru.mishapp.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.mishapp.dto.PeriodicChangeRuleDTO;
import ru.mishapp.entity.Account;
import ru.mishapp.entity.PeriodicChange;
import ru.mishapp.entity.PeriodicChangeRule;
import ru.mishapp.repository.AccountRepository;
import ru.mishapp.repository.PeriodicChangeRepository;
import ru.mishapp.repository.PeriodicChangeRuleRepository;

import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PeriodicChangeRuleService {
    
    private final PeriodicChangeRuleRepository periodicChangeRuleRepository;
    private final AccountRepository accountRepository;
    private final PeriodicChangeRepository periodicChangeRepository;
    
    public PeriodicChangeRule create(PeriodicChangeRuleDTO rule, long chatId) {
        PeriodicChangeRule.PeriodicChangeRuleBuilder builder = PeriodicChangeRule.builder();
        Optional<Account> targetAccount = accountRepository.findByNameAndChatId(rule.taName(), chatId);
        if (targetAccount.isEmpty()) {
            throw new IllegalArgumentException("Не существует счёта с именем " + rule.taName());
        }
        builder.targetAccountId(targetAccount.get().getId());
        
        if (StringUtils.hasText(rule.recName())) {
            Optional<Account> recAccount = accountRepository.findByNameAndChatId(rule.recName(), chatId);
            if (recAccount.isEmpty()) {
                throw new IllegalArgumentException("Не существует счёта с именем " + rule.recName());
            }
            builder.receivingAccountId(recAccount.get().getId());
        }
        
        Optional<PeriodicChange> periodicChange = periodicChangeRepository.findByNameAndChatId(rule.pcName(), chatId);
        if (periodicChange.isEmpty()) {
            throw new IllegalArgumentException("Не существует периодического изменения " + rule.pcName());
        }
        builder.periodicChangeId(periodicChange.get().getId());
        
        Optional<PeriodicChangeRule.Type> type = PeriodicChangeRule.Type.of(rule.type());
        if (type.isEmpty()) {
            throw new IllegalArgumentException("Не существует типа правила " + rule.type());
        }
        builder.type(type.get());
        builder.name(rule.name());
        builder.sum(rule.sum());
        builder.pass(rule.pass());
        builder.nextDay(rule.startDay());
        
        return periodicChangeRuleRepository.save(builder.build());
    }
    
    public Set<PeriodicChangeRule> readAllByName(String name, Long chatId) {
        return periodicChangeRepository.findByNameAndChatId(name, chatId)
            .map(PeriodicChange::getRules)
            .orElse(Set.of());
    }
}
