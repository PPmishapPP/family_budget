package ru.mishapp.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mishapp.api.ApiPaths;
import ru.mishapp.api.dto.ForecastItemDto;
import ru.mishapp.api.dto.ForecastRequest;
import ru.mishapp.api.dto.IncomeForecastItemDto;
import ru.mishapp.api.dto.IncomeForecastRequest;
import ru.mishapp.api.mapper.ForecastApiMapper;
import ru.mishapp.api.security.AccountAccessService;
import ru.mishapp.services.ForecastService;
import ru.mishapp.services.records.CalcItem;

import java.util.List;

@RestController
@RequestMapping(ApiPaths.V1 + "/accounts/{accountId}")
@RequiredArgsConstructor
public class ForecastController {

    private final ForecastService forecastService;
    private final AccountAccessService accessService;
    private final ForecastApiMapper mapper;

    @PostMapping("/forecast")
    public List<ForecastItemDto> forecast(
            @PathVariable Long accountId,
            @Valid @RequestBody ForecastRequest request) {
        accessService.requireAccount(accountId);
        List<CalcItem> items = forecastService.calculateForecast(
                accountId, request.to(), mapper.toLegacy(request.rules()));
        return items.stream()
                .map(mapper::toDto)
                .toList();
    }

    @PostMapping("/income-forecast")
    public List<IncomeForecastItemDto> incomeForecast(
            @PathVariable Long accountId,
            @Valid @RequestBody IncomeForecastRequest request) {
        accessService.requireAccount(accountId);
        return forecastService.forecastIncomeRaw(request.to(), accountId).stream()
                .map(mapper::toDto)
                .toList();
    }
}
