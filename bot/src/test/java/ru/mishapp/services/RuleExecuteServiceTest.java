package ru.mishapp.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mishapp.entity.PeriodicChangeRule;
import ru.mishapp.enumiration.Type;
import ru.mishapp.mapper.PeriodicChangeRuleMapper;
import ru.mishapp.repository.PeriodicChangeRuleRepository;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RuleExecuteServiceTest {

    @Mock
    private PeriodicChangeRuleRepository repository;

    @Mock
    private AccountService accountService;

    @Mock
    private PeriodicChangeRuleMapper mapper;

    @InjectMocks
    private RuleExecuteService service;

    @Test
    void shouldApplyRuleAndMoveNextDay() {
        PeriodicChangeRule rule = rule(Type.MONTHLY, LocalDate.of(2026, 12, 1), null);
        when(repository.findByIdAndTargetAccountId(5L, 10L)).thenReturn(Optional.of(rule));
        when(repository.save(any(PeriodicChangeRule.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.execute(10L, 5L);

        verify(accountService).applyRule(rule);
        ArgumentCaptor<PeriodicChangeRule> captor = ArgumentCaptor.forClass(PeriodicChangeRule.class);
        verify(repository).save(captor.capture());
        assertEquals(LocalDate.of(2027, 1, 1), captor.getValue().getNextDay());
    }

    @Test
    void shouldNotRescheduleOneTimeRule() {
        PeriodicChangeRule rule = rule(Type.ONE, LocalDate.of(2026, 12, 1), null);
        when(repository.findByIdAndTargetAccountId(5L, 10L)).thenReturn(Optional.of(rule));

        service.execute(10L, 5L);

        verify(accountService).applyRule(rule);
        verify(repository, never()).save(any(PeriodicChangeRule.class));
    }

    @Test
    void shouldMoveNextDayPastEndDate() {
        PeriodicChangeRule rule = rule(Type.MONTHLY, LocalDate.of(2026, 12, 1),
                LocalDate.of(2026, 12, 15));
        when(repository.findByIdAndTargetAccountId(5L, 10L)).thenReturn(Optional.of(rule));
        when(repository.save(any(PeriodicChangeRule.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.execute(10L, 5L);

        ArgumentCaptor<PeriodicChangeRule> captor = ArgumentCaptor.forClass(PeriodicChangeRule.class);
        verify(repository).save(captor.capture());
        assertEquals(LocalDate.of(2027, 1, 1), captor.getValue().getNextDay());
    }

    private PeriodicChangeRule rule(Type type, LocalDate nextDay, LocalDate endDate) {
        return PeriodicChangeRule.builder()
                .id(5L)
                .targetAccountId(10L)
                .name("Интернет")
                .sum(-500)
                .type(type)
                .pass(0)
                .nextDay(nextDay)
                .endDate(endDate)
                .build();
    }
}
