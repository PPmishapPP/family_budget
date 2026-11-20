package ru.mishapp.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mishapp.dto.PeriodicChangeRuleDto;
import ru.mishapp.entity.PeriodicChangeRule;
import ru.mishapp.mapper.PeriodicChangeRuleMapper;
import ru.mishapp.repository.PeriodicChangeRuleRepository;
import ru.mishapp.services.records.PaymentRuleCommand;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class PeriodicChangeRuleService {

    private final PeriodicChangeRuleRepository periodicChangeRuleRepository;
    private final PeriodicChangeRuleMapper periodicChangeRuleMapper;

    public List<PeriodicChangeRuleDto> findAllPeriodicChangeRuleDtos(Long accountId) {
        return periodicChangeRuleRepository.findAllRuleDtos(accountId);
    }

    public void saveAll(List<PeriodicChangeRuleDto> dtos, Long accountId) {
        periodicChangeRuleRepository.saveAll(dtos.stream()
                .map(dto -> periodicChangeRuleMapper.toEntity(dto, accountId))
                .toList());
    }

    public void deleteAll(List<PeriodicChangeRuleDto> dtos) {
        periodicChangeRuleRepository.deleteAllById(dtos.stream().map(PeriodicChangeRuleDto::id).toList());
    }

    @Transactional(readOnly = true)
    public List<PeriodicChangeRuleDto> findAll(long accountId) {
        return periodicChangeRuleRepository.findAllRuleDtos(accountId);
    }

    @Transactional
    public PeriodicChangeRule create(long accountId, PaymentRuleCommand command) {
        return periodicChangeRuleRepository.save(toEntity(null, accountId, 0, command));
    }

    @Transactional
    public PeriodicChangeRule update(long accountId, long ruleId, PaymentRuleCommand command) {
        PeriodicChangeRule existing = periodicChangeRuleRepository
                .findByIdAndTargetAccountId(ruleId, accountId)
                .orElseThrow(() -> new NoSuchElementException("Правило не найдено"));
        return periodicChangeRuleRepository.save(
                toEntity(existing.getId(), accountId, existing.getPass(), command));
    }

    @Transactional
    public void delete(long accountId, long ruleId) {
        PeriodicChangeRule existing = periodicChangeRuleRepository
                .findByIdAndTargetAccountId(ruleId, accountId)
                .orElseThrow(() -> new NoSuchElementException("Правило не найдено"));
        periodicChangeRuleRepository.deleteById(existing.getId());
    }

    private PeriodicChangeRule toEntity(Long id, long accountId, int pass, PaymentRuleCommand command) {
        return PeriodicChangeRule.builder()
                .id(id)
                .targetAccountId(accountId)
                .name(command.name().trim())
                .sum(command.sum())
                .type(command.type())
                .pass(pass)
                .nextDay(command.nextDay())
                .endDate(command.endDate())
                .build();
    }
}
