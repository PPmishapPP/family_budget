package ru.mishapp.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mishapp.api.ApiPaths;
import ru.mishapp.api.dto.PaymentRuleDto;
import ru.mishapp.api.dto.PaymentRuleRequest;
import ru.mishapp.api.mapper.PaymentRuleApiMapper;
import ru.mishapp.api.security.AccountAccessService;
import ru.mishapp.entity.PeriodicChangeRule;
import ru.mishapp.services.PeriodicChangeRuleService;
import ru.mishapp.services.RuleExecuteService;

import java.util.List;

@RestController
@RequestMapping(ApiPaths.V1 + "/accounts/{accountId}/payment-rules")
@RequiredArgsConstructor
public class PaymentRuleController {

    private final PeriodicChangeRuleService periodicChangeRuleService;
    private final RuleExecuteService ruleExecuteService;
    private final AccountAccessService accessService;
    private final PaymentRuleApiMapper mapper;

    @GetMapping
    public List<PaymentRuleDto> findAll(@PathVariable Long accountId) {
        accessService.requireAccount(accountId);
        return periodicChangeRuleService.findAll(accountId).stream()
                .map(mapper::toDto)
                .toList();
    }

    @PostMapping
    public ResponseEntity<PaymentRuleDto> create(
            @PathVariable Long accountId,
            @Valid @RequestBody PaymentRuleRequest request) {
        accessService.requireAccount(accountId);
        PeriodicChangeRule created = periodicChangeRuleService.create(accountId, mapper.toCommand(request));
        return ResponseEntity.created(ApiPaths.paymentRule(accountId, created.getId()))
                .body(mapper.toDto(created));
    }

    @PutMapping("/{ruleId}")
    public PaymentRuleDto update(
            @PathVariable Long accountId,
            @PathVariable Long ruleId,
            @Valid @RequestBody PaymentRuleRequest request) {
        accessService.requireAccount(accountId);
        return mapper.toDto(periodicChangeRuleService.update(accountId, ruleId, mapper.toCommand(request)));
    }

    @DeleteMapping("/{ruleId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long accountId,
            @PathVariable Long ruleId) {
        accessService.requireAccount(accountId);
        periodicChangeRuleService.delete(accountId, ruleId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{ruleId}/execute")
    public ResponseEntity<Void> execute(
            @PathVariable Long accountId,
            @PathVariable Long ruleId) {
        accessService.requireAccount(accountId);
        ruleExecuteService.execute(accountId, ruleId);
        return ResponseEntity.noContent().build();
    }
}
