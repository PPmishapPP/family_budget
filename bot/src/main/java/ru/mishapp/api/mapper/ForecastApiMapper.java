package ru.mishapp.api.mapper;

import org.springframework.stereotype.Component;
import ru.mishapp.api.dto.ForecastItemDto;
import ru.mishapp.api.dto.IncomeForecastItemDto;
import ru.mishapp.api.dto.PaymentRuleRequest;
import ru.mishapp.dto.PeriodicChangeRuleDto;
import ru.mishapp.services.records.CalcItem;
import ru.mishapp.services.records.IncomeCalcItem;

import java.util.List;

@Component
public class ForecastApiMapper {

    public List<PeriodicChangeRuleDto> toLegacy(List<PaymentRuleRequest> rules) {
        return rules == null ? null : rules.stream().map(this::toLegacy).toList();
    }

    public PeriodicChangeRuleDto toLegacy(PaymentRuleRequest request) {
        return new PeriodicChangeRuleDto(
                null,
                request.name().trim(),
                request.sum(),
                request.type().name(),
                0,
                request.nextDay(),
                request.endDate()
        );
    }

    public ForecastItemDto toDto(CalcItem item) {
        return new ForecastItemDto(
                item.day(),
                item.rule().name(),
                item.rule().sum(),
                item.balance()
        );
    }

    public IncomeForecastItemDto toDto(IncomeCalcItem item) {
        return new IncomeForecastItemDto(item.date(), item.balance(), item.increase());
    }
}
