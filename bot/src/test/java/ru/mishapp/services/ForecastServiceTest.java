package ru.mishapp.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mishapp.dto.PeriodicChangeRuleDto;
import ru.mishapp.services.records.CalcItem;
import ru.mishapp.services.records.IncomeCalcItem;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ForecastServiceTest {

    @Mock
    private ForecastCalculator forecastCalculator;

    @InjectMocks
    private ForecastService forecastService;

    @Test
    void shouldReturnNumericIncomeForecastWithoutFormatting() {
        LocalDate day1 = LocalDate.of(2026, 11, 17);
        LocalDate day2 = LocalDate.of(2026, 11, 18);
        LocalDate day3 = LocalDate.of(2026, 11, 19);
        LocalDate day4 = LocalDate.of(2026, 11, 20);
        LocalDate to = day4;
        PeriodicChangeRuleDto rule = new PeriodicChangeRuleDto(
                1L, "Доход", 100, "MONTHLY", 0, day1, true, null);

        when(forecastCalculator.calc(10L, to)).thenReturn(List.of(
                new CalcItem(day1, 100, rule),
                new CalcItem(day2, 50, rule),
                new CalcItem(day3, 300, rule),
                new CalcItem(day4, 200, rule)
        ));

        List<IncomeCalcItem> result = forecastService.forecastIncomeRaw(to, 10L);

        assertEquals(2, result.size());
        assertEquals(day1, result.get(0).date());
        assertEquals(50, result.get(0).balance());
        assertNull(result.get(0).increase());
        assertEquals(day3, result.get(1).date());
        assertEquals(200, result.get(1).balance());
        assertEquals(150, result.get(1).increase());
    }

    @Test
    void shouldReturnEmptyIncomeForecastWhenThereAreNoRules() {
        LocalDate to = LocalDate.of(2026, 11, 20);
        when(forecastCalculator.calc(10L, to)).thenReturn(List.of());

        assertTrue(forecastService.forecastIncomeRaw(to, 10L).isEmpty());
    }
}
