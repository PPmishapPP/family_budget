package ru.mishapp.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.mishapp.dto.PeriodicChangeRuleDto;
import ru.mishapp.entity.Account;
import ru.mishapp.entity.PeriodicChange;
import ru.mishapp.entity.PeriodicChangeRule;
import ru.mishapp.mapper.PeriodicChangeRuleMapper;
import ru.mishapp.repository.AccountRepository;
import ru.mishapp.repository.PeriodicChangeRepository;
import ru.mishapp.repository.PeriodicChangeRuleRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PeriodicChangeRuleService {
    
    private final PeriodicChangeRuleRepository periodicChangeRuleRepository;
    private final PeriodicChangeRuleMapper mapper;
    private final PeriodicChangeRepository periodicChangeRepository;
    private final AccountRepository accountRepository;
    private final PeriodicChangeService periodicChangeService;
    
    public PeriodicChangeRule create(PeriodicChangeRuleDto dto, long chatId) {
        Optional<Account> targetAccount = accountRepository.findByNameAndChatId(dto.targetAccountName(), chatId);
        if (targetAccount.isEmpty()) {
            throw new IllegalArgumentException("Не существует счёта с именем " + dto.targetAccountName());
        }
        Map<String, PeriodicChange> periodicChangeMap = periodicChangeService.findAll(chatId);
        return periodicChangeRuleRepository.save(mapper.toEntity(dto, targetAccount.get(), periodicChangeMap));
    }

    public void saveAll(List<PeriodicChangeRuleDto> dtos, long chatId) {
        periodicChangeRuleRepository.saveAll(mapper.toEntityList(dtos, chatId));
    }
    
    public Set<PeriodicChangeRule> readAllByName(String name, Long chatId) {
        return periodicChangeRepository.findByNameAndChatId(name, chatId)
            .map(PeriodicChange::getRules)
            .orElse(Set.of());
    }

    public List<PeriodicChangeRuleDto> findAllPeriodicChangeRuleDtos(long chatId) {
        return periodicChangeRuleRepository.findAllRuleDtos(chatId);
    }

    public void deleteAll(List<PeriodicChangeRuleDto> dtos, long chatId) {
        List<PeriodicChangeRule> entityList = mapper.toEntityList(dtos, chatId);
        periodicChangeRuleRepository.deleteAllById(entityList.stream().map(PeriodicChangeRule::getId).toList());
    }
}
