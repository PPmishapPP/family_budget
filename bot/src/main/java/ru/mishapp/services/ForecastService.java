package ru.mishapp.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.mishapp.dto.ListDto;
import ru.mishapp.entity.Account;
import ru.mishapp.services.records.CalcItem;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ru.mishapp.Constants.DAY;
import static ru.mishapp.Constants.RUB;

@Service
@RequiredArgsConstructor
public class ForecastService {

    private final ForecastCalculator forecastCalculator;

    public ListDto forecastTo(LocalDate to, Account account, Long chatId) {
        List<String> result = forecastCalculator.calc(account, to, chatId)
            .stream()
            .map(calcItem -> String.format(
                "%s: %s₽ %s %s",
                calcItem.day().format(DAY),
                RUB.format(calcItem.balance()),
                calcItem.rule().getName(),
                RUB.format(calcItem.rule().getSum()))
            )
            .collect(Collectors.toList());
        
        return new ListDto(result);
    }
    
    public ListDto forecastIncome(LocalDate to, Account account, Long chatId) {
        List<CalcItem> calcItems = forecastCalculator.calc(account, to, chatId);
        
        List<String> result = new ArrayList<>();
        int oldMin = -1;
        while (true) {
            int min = calcItems.get(0).balance();
            int minIndex = 0;
            for (int i = 1; i < calcItems.size(); i++) {
                CalcItem calcItem = calcItems.get(i);
                if (calcItem.balance() <= min) {
                    min = calcItem.balance();
                    minIndex = i;
                }
            }
            
            if (min > 0 && minIndex > 0) {
                CalcItem income = calcItems.get(0);
                for (int i = minIndex; i >= 0; i--) {
                    CalcItem calcItem = calcItems.get(i);
                    if (calcItem.balance() - min < 0) {
                        income = calcItems.get(i - 1);
                        break;
                    }
                }
                if (oldMin == -1) {
                    result.add(String.format(
                        "%s: %s₽",
                        income.day().format(DAY),
                        RUB.format(min)
                    ));
                } else {
                    result.add(String.format(
                        "%s: %s₽ (+%s)",
                        income.day().format(DAY),
                        RUB.format(min),
                        RUB.format((long) min - oldMin)
                    ));
                }
                oldMin = min;
            }
            
            if (minIndex + 1 == calcItems.size()) {
                return new ListDto(result);
            }
            
            calcItems = calcItems.subList(minIndex + 1, calcItems.size());
        }
    }
}
