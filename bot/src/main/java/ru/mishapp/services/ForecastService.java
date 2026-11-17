package ru.mishapp.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.mishapp.dto.IncomeDto;
import ru.mishapp.dto.PeriodicChangeRuleDto;
import ru.mishapp.services.records.CalcItem;
import ru.mishapp.services.records.IncomeCalcItem;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static ru.mishapp.Constants.DAY;
import static ru.mishapp.Constants.RUB;

@Service
@RequiredArgsConstructor
public class ForecastService {

    private final ForecastCalculator forecastCalculator;

    public List<CalcItem> calculateForecast(List<PeriodicChangeRuleDto> dtos, Long accountId, LocalDate to) {
        return forecastCalculator.calc(dtos, accountId, to);
    }

    public List<CalcItem> calculateForecast(Long accountId, LocalDate to) {
        return forecastCalculator.calc(accountId, to);
    }

    public List<CalcItem> calculateForecast(Long accountId, LocalDate to, List<PeriodicChangeRuleDto> rules) {
        return rules == null
                ? forecastCalculator.calc(accountId, to)
                : forecastCalculator.calc(rules, accountId, to);
    }

    public List<IncomeDto> forecastIncome(LocalDate to, long accountId) {
        return forecastIncomeRaw(to, accountId).stream()
                .map(item -> new IncomeDto(
                        item.date().format(DAY),
                        RUB.format(item.balance()),
                        item.increase() == null ? null : "+" + RUB.format(item.increase())
                ))
                .toList();
    }

    public List<IncomeCalcItem> forecastIncomeRaw(LocalDate to, long accountId) {
        List<CalcItem> calcItems = forecastCalculator.calc(accountId, to);
        if (calcItems.isEmpty()) {
            return List.of();
        }

        List<IncomeCalcItem> result = new ArrayList<>();
        int oldMin = -1;
        while (true) {
            int min = calcItems.getFirst().balance();
            int minIndex = 0;
            for (int i = 1; i < calcItems.size(); i++) {
                CalcItem calcItem = calcItems.get(i);
                if (calcItem.balance() <= min) {
                    min = calcItem.balance();
                    minIndex = i;
                }
            }

            if (min > 0 && minIndex > 0) {
                CalcItem income = calcItems.getFirst();
                for (int i = minIndex; i >= 0; i--) {
                    CalcItem calcItem = calcItems.get(i);
                    if (calcItem.balance() - min < 0) {
                        income = calcItems.get(i - 1);
                        break;
                    }
                }
                result.add(new IncomeCalcItem(
                        income.day(),
                        min,
                        oldMin == -1 ? null : min - oldMin
                ));
                oldMin = min;
            }

            if (minIndex + 1 == calcItems.size()) {
                return result;
            }

            calcItems = calcItems.subList(minIndex + 1, calcItems.size());
        }
    }
}
