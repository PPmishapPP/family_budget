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

    public PeriodicChangeRule toEntity(PeriodicChangeRuleDto dto) {
        PeriodicChangeRule.PeriodicChangeRuleBuilder builder = PeriodicChangeRule.builder();
        builder.targetAccountId(dto.targetAccountId());

        Type type = Type.valueOf(dto.type());
        builder.id(dto.id());
        builder.type(type);
        builder.name(dto.name());
        builder.sum(dto.sum());
        builder.pass(dto.pass());
        builder.nextDay(dto.nextDay());
        builder.active(dto.active());
        builder.endDate(dto.endDate());
        return builder.build();
    }

    public List<PeriodicChangeRule> toEntityList(List<PeriodicChangeRuleDto> dtos) {
        return dtos.stream()
                .map(this::toEntity)
                .toList();
    }
}