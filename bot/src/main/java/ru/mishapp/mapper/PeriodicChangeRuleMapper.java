package ru.mishapp.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.mishapp.dto.PeriodicChangeRuleDto;
import ru.mishapp.entity.PeriodicChangeRule;
import ru.mishapp.enumiration.Type;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PeriodicChangeRuleMapper {

    public PeriodicChangeRule toEntity(PeriodicChangeRuleDto dto, Long accountId) {
        PeriodicChangeRule.PeriodicChangeRuleBuilder builder = PeriodicChangeRule.builder();

        builder.id(dto.id());
        builder.type(Type.valueOf(dto.type()));
        builder.name(dto.name());
        builder.sum(dto.sum());
        builder.pass(dto.pass());
        builder.nextDay(dto.nextDay());
        builder.active(dto.active());
        builder.endDate(dto.endDate());
        builder.targetAccountId(accountId);
        return builder.build();
    }

    public List<PeriodicChangeRule> toEntityList(List<PeriodicChangeRuleDto> dtos, Long accountId) {
        return dtos.stream()
                .map(dto -> toEntity(dto, accountId))
                .toList();
    }
}