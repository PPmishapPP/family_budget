package ru.mishapp.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.mishapp.api.ApiPaths;
import ru.mishapp.api.dto.AccountHistoryDto;
import ru.mishapp.api.dto.PageResponse;
import ru.mishapp.api.mapper.AccountApiMapper;
import ru.mishapp.api.security.AccountAccessService;
import ru.mishapp.services.HistoryService;

@RestController
@RequestMapping(ApiPaths.V1 + "/accounts/{accountId}/history")
@RequiredArgsConstructor
public class HistoryController {

    private final AccountAccessService accessService;
    private final HistoryService historyService;
    private final AccountApiMapper mapper;

    @GetMapping
    public PageResponse<AccountHistoryDto> history(
            @PathVariable Long accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        accessService.requireAccount(accountId);
        return mapper.toPageResponse(historyService.page(accountId, page, size));
    }
}
