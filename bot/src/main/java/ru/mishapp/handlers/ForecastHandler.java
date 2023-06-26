package ru.mishapp.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.mishapp.Constants;
import ru.mishapp.annotations.TelegramCommand;
import ru.mishapp.annotations.TelegramHandler;
import ru.mishapp.annotations.TelegramParam;
import ru.mishapp.dto.ListDto;
import ru.mishapp.entity.Account;
import ru.mishapp.services.AccountService;
import ru.mishapp.services.ForecastService;
import ru.mishapp.services.records.ForecastResult;

import java.time.LocalDate;

@Service
@TelegramHandler(value = "прогноз", description = "Узнать прогноз на какую-то дату")
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class ForecastHandler {
    
    private final ForecastService forecastService;
    private final AccountService accountService;
    
    @TelegramCommand("на")
    public String forecastFor(@TelegramParam("дату") String date, Long chatId) {
        LocalDate day = LocalDate.parse(date, Constants.DAY);
        ForecastResult forecastResult = forecastService.forecastFor(day, chatId);
        return forecastResult.toTelegram();
    }
    
    @TelegramCommand("до")
    public String forecastTo(
        @TelegramParam("даты") String date,
        @TelegramParam("счёт") String accountName,
        Long chatId
    ) {
        LocalDate day = LocalDate.parse(date, Constants.DAY);
        Account account = accountService.readByName(accountName, chatId);
        ListDto listDto = forecastService.forecastTo(day, account, chatId);
        return listDto.toTelegram();
    }
    
    @TelegramCommand("доход")
    public String forecastIncome(
        @TelegramParam("дата") String date,
        @TelegramParam("счёт") String accountName,
        Long chatId
    ) {
        LocalDate day = LocalDate.parse(date, Constants.DAY);
        Account account = accountService.readByName(accountName, chatId);
        ListDto listDto = forecastService.forecastIncome(day, account, chatId);
        return listDto.toTelegram();
    }
}
