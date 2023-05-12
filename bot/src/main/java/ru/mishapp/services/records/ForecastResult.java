package ru.mishapp.services.records;

import java.util.List;

public record ForecastResult(List<ForecastItem> rules, List<ForecastItem> accounts) {
    
    public String toTelegram() {
        StringBuilder builder = new StringBuilder();
        for (ForecastItem rule : rules) {
            builder.append(rule.name());
            builder.append(": ");
            builder.append(rule.sum());
            builder.append("₽\n");
        }
        builder.append("-----------------\n");
        for (ForecastItem account : accounts) {
            builder.append(account.name());
            builder.append(": ");
            builder.append(account.sum());
            builder.append("₽\n");
        }
        
        return builder.toString();
    }
}
