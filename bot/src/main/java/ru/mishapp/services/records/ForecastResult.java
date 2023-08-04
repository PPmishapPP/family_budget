package ru.mishapp.services.records;

import java.util.List;
import ru.mishapp.Constants;

public record ForecastResult(List<ForecastItem> rules, List<ForecastItem> accounts) {
    
    public String toTelegram() {
        StringBuilder builder = new StringBuilder();
        for (ForecastItem rule : rules) {
            builder.append(rule.name());
            builder.append(": ");
            builder.append(Constants.RUB.format(rule.sum()));
            builder.append("₽\n");
        }
        builder.append("-----------------\n");
        for (ForecastItem account : accounts) {
            builder.append(account.name());
            builder.append(": ");
            builder.append(Constants.RUB.format(account.sum()));
            builder.append("₽\n");
        }
        
        return builder.toString();
    }
}
