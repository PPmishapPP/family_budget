package ru.mishapp.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mishapp.dto.PeriodicChangeRuleDto;
import ru.mishapp.entity.PeriodicChangeRule;
import ru.mishapp.mapper.PeriodicChangeRuleMapper;
import ru.mishapp.repository.PeriodicChangeRuleRepository;

import java.time.LocalDate;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class RuleExecuteService {

    private final PeriodicChangeRuleRepository periodicChangeRuleRepository;
    private final AccountService accountService;
    private final PeriodicChangeRuleMapper mapper;

    @Transactional
    public void ruleExecute(PeriodicChangeRuleDto dto, Long accountId) {
        execute(mapper.toEntity(dto, accountId));
    }

    @Transactional
    public void execute(long accountId, long ruleId) {
        PeriodicChangeRule entity = periodicChangeRuleRepository
                .findByIdAndTargetAccountId(ruleId, accountId)
                .orElseThrow(() -> new NoSuchElementException("Правило не найдено"));
        execute(entity);
    }

    private void execute(PeriodicChangeRule entity) {
        accountService.applyRule(entity);
        LocalDate nextDay = entity.getType().next(entity.getNextDay(), entity.getPass());

        if (nextDay != null && !nextDay.isEqual(entity.getNextDay())) {
            periodicChangeRuleRepository.save(entity.withNextDay(nextDay));
        }
    }
}
