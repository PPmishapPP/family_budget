package ru.mishapp.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mishapp.api.ApiPaths;
import ru.mishapp.api.dto.AccountHistoryDto;
import ru.mishapp.api.dto.InstantPaymentRequest;
import ru.mishapp.api.mapper.AccountApiMapper;
import ru.mishapp.api.security.AccountAccessService;
import ru.mishapp.entity.AccountHistory;
import ru.mishapp.services.AccountService;

@RestController
@RequestMapping(ApiPaths.V1 + "/accounts/{accountId}/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final AccountService accountService;
    private final AccountAccessService accessService;
    private final AccountApiMapper mapper;

    @PostMapping
    public ResponseEntity<AccountHistoryDto> registerPayment(
            @PathVariable Long accountId,
            @Valid @RequestBody InstantPaymentRequest request) {
        accessService.requireAccount(accountId);
        AccountHistory payment = accountService.registerPayment(
                accountId, request.name(), request.amount(), request.direction());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDto(payment));
    }
}
