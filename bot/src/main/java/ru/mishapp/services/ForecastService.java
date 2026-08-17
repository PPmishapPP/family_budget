package ru.mishapp.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.mishapp.dto.IncomeDto;
import ru.mishapp.dto.PeriodicChangeRuleDto;
import ru.mishapp.services.records.CalcItem;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static ru.mishapp.Constants.DAY;
import static ru.mishapp.Constants.RUB;

@Service
@RequiredArgsConstructor
public class ForecastService {

    private final ForecastCalculator forecastCalculator;

    public List<CalcItem> calculateForecast(List<PeriodicChangeRuleDto> dtos, long chatId, LocalDate to) {
        return forecastCalculator.calc(dtos, to);
    }

    public List<IncomeDto> forecastIncome(LocalDate to, String accountName, Long chatId) {
        List<CalcItem> calcItems = forecastCalculator.calc(accountName, to, chatId);

        List<IncomeDto> result = new ArrayList<>();
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
                if (oldMin == -1) {
                    result.add(new IncomeDto(income.day().format(DAY),
                            RUB.format(min), null));

                } else {
                    result.add(new IncomeDto(income.day().format(DAY),
                            RUB.format(min), "+" + RUB.format((long) min - oldMin)));
                }
                oldMin = min;
            }

            if (minIndex + 1 == calcItems.size()) {
                return result;
            }

            calcItems = calcItems.subList(minIndex + 1, calcItems.size());
        }
    }
}
