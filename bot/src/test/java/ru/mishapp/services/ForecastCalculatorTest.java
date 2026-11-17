package ru.mishapp.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mishapp.repository.AccountHistoryRepository;
import ru.mishapp.repository.PeriodicChangeRuleRepository;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ForecastCalculatorTest {

    @Mock
    private PeriodicChangeRuleRepository periodicChangeRuleRepository;

    @Mock
    private AccountHistoryRepository accountHistoryRepository;

    @InjectMocks
    private ForecastCalculator calculator;

    @Test
    void shouldStartFromZeroWhenAccountHasNoHistory() {
        when(periodicChangeRuleRepository.findAllRuleDtos(10L)).thenReturn(List.of());
        when(accountHistoryRepository.findLastByAccountId(10L)).thenReturn(null);

        assertTrue(calculator.calc(10L, LocalDate.now()).isEmpty());
    }
}
