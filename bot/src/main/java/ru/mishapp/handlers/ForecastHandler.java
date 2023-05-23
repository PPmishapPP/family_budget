package ru.mishapp.handlers;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.mishapp.Constants;
import ru.mishapp.annotations.TelegramCommand;
import ru.mishapp.annotations.TelegramHandler;
import ru.mishapp.annotations.TelegramParam;
import ru.mishapp.services.ForecastService;
import ru.mishapp.services.records.ForecastResult;

@Service
@TelegramHandler(value = "прогноз", description = "Узнать прогноз на какую-то дату")
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class ForecastHandler {
    
    private final ForecastService forecastService;
    
    @TelegramCommand("на")
    public String forecastFor(@TelegramParam("дату") String date, Long chatId) {
        LocalDate day = LocalDate.parse(date, Constants.DAY);
        ForecastResult forecastResult = forecastService.forecastFor(day, chatId);
        return forecastResult.toTelegram();
    }
}
