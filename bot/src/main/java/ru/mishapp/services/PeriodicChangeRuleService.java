package ru.mishapp.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.mishapp.dto.PeriodicChangeRuleDto;
import ru.mishapp.mapper.PeriodicChangeRuleMapper;
import ru.mishapp.repository.PeriodicChangeRuleRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PeriodicChangeRuleService {

    private final PeriodicChangeRuleRepository periodicChangeRuleRepository;
    private final PeriodicChangeRuleMapper mapper;

    public void saveAll(List<PeriodicChangeRuleDto> dtos) {
        periodicChangeRuleRepository.saveAll(mapper.toEntityList(dtos));
    }

    public List<PeriodicChangeRuleDto> findAllPeriodicChangeRuleDtos(long accountId) {
        return periodicChangeRuleRepository.findAllRuleDtos(accountId);
    }

    public void deleteAll(List<PeriodicChangeRuleDto> dtos) {
        periodicChangeRuleRepository.deleteAllById(dtos.stream().map(PeriodicChangeRuleDto::id).toList());
    }
}
