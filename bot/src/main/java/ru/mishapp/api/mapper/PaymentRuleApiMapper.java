package ru.mishapp.api.mapper;

import org.springframework.stereotype.Component;
import ru.mishapp.api.dto.PaymentRuleDto;
import ru.mishapp.api.dto.PaymentRuleRequest;
import ru.mishapp.dto.PeriodicChangeRuleDto;
import ru.mishapp.entity.PeriodicChangeRule;
import ru.mishapp.enumiration.Type;
import ru.mishapp.services.records.PaymentRuleCommand;

@Component
public class PaymentRuleApiMapper {

    public PaymentRuleCommand toCommand(PaymentRuleRequest request) {
        return new PaymentRuleCommand(
                request.name(),
                request.sum(),
                request.type(),
                request.nextDay(),
                request.active(),
                request.endDate()
        );
    }

    public PaymentRuleDto toDto(PeriodicChangeRule rule) {
        return new PaymentRuleDto(
                rule.getId(),
                rule.getName(),
                rule.getSum(),
                rule.getType(),
                rule.getPass(),
                rule.getNextDay(),
                rule.isActive(),
                rule.getEndDate()
        );
    }

    public PaymentRuleDto toDto(PeriodicChangeRuleDto dto) {
        return new PaymentRuleDto(
                dto.id(),
                dto.name(),
                dto.sum(),
                Type.valueOf(dto.type()),
                dto.pass(),
                dto.nextDay(),
                dto.active(),
                dto.endDate()
        );
    }
}
