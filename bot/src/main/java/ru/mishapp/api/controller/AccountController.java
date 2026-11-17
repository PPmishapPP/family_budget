package ru.mishapp.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mishapp.api.ApiPaths;
import ru.mishapp.api.dto.AccountDto;
import ru.mishapp.api.dto.BalanceDto;
import ru.mishapp.api.mapper.AccountApiMapper;
import ru.mishapp.api.security.AccountAccessService;
import ru.mishapp.services.AccountService;

import java.util.List;

@RestController
@RequestMapping(ApiPaths.V1 + "/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final AccountAccessService accessService;
    private final AccountApiMapper mapper;

    @GetMapping
    public List<AccountDto> findAll() {
        return accountService.readAllAccounts().stream()
                .map(mapper::toDto)
                .toList();
    }

    @GetMapping("/{accountId}")
    public AccountDto findById(@PathVariable Long accountId) {
        return mapper.toDto(accessService.requireAccount(accountId));
    }

    @GetMapping("/{accountId}/balance")
    public BalanceDto balance(@PathVariable Long accountId) {
        accessService.requireAccount(accountId);
        return mapper.toBalanceDto(accountId, accountService.getBalance(accountId));
    }
}
