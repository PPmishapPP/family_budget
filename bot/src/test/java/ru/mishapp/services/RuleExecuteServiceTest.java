package ru.mishapp.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mishapp.entity.PeriodicChange;
import ru.mishapp.entity.PeriodicChangeRule;
import ru.mishapp.enumiration.Type;
import ru.mishapp.repository.PeriodicChangeRepository;
import ru.mishapp.repository.PeriodicChangeRuleRepository;
import ru.mishapp.services.records.ApplyResult;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.mishapp.enumiration.Type.DAILY;
import static ru.mishapp.enumiration.Type.MONTHLY;
import static ru.mishapp.enumiration.Type.ONE;
import static ru.mishapp.enumiration.Type.WEEKLY;
import static ru.mishapp.enumiration.Type.YEARLY;

@ExtendWith(MockitoExtension.class)
class RuleExecuteServiceTest {

	@InjectMocks
	RuleExecuteService ruleExecuteService;

	@Mock
	AccountService accountService;

	@Mock
	PeriodicChangeRuleRepository periodicChangeRuleRepository;

	@Mock
	PeriodicChangeRepository periodicChangeRepository;

	@Test
	public void shouldDeactivateMonthlyRule() {
		PeriodicChangeRule periodicChangeRule = createPeriodicChangeRule(MONTHLY, LocalDate.of(2026, 1, 13));
		mock(periodicChangeRule);
		ArgumentCaptor<PeriodicChangeRule> argumentCaptor = ArgumentCaptor.forClass(PeriodicChangeRule.class);
		ruleExecuteService.ruleExecute(LocalDate.of(2026, 1, 13));
		verify(periodicChangeRuleRepository).save(argumentCaptor.capture());
		PeriodicChangeRule argumentCaptorValue = argumentCaptor.getValue();
		assertFalse(argumentCaptorValue.isActive());
		assertEquals(LocalDate.of(2026, 2, 12), argumentCaptorValue.getNextDay());
	}

	@Test
	public void shouldNotDeactivateMonthlyRule() {
		PeriodicChangeRule periodicChangeRule = createPeriodicChangeRule(MONTHLY, LocalDate.of(2026, 10, 13));
		mock(periodicChangeRule);
		ArgumentCaptor<PeriodicChangeRule> argumentCaptor = ArgumentCaptor.forClass(PeriodicChangeRule.class);
		ruleExecuteService.ruleExecute(LocalDate.of(2026, 1, 13));
		verify(periodicChangeRuleRepository).save(argumentCaptor.capture());
		PeriodicChangeRule argumentCaptorValue = argumentCaptor.getValue();
		assertTrue(argumentCaptorValue.isActive());
		assertEquals(LocalDate.of(2026, 2, 12), argumentCaptorValue.getNextDay());
	}

	@Test
	public void shouldDeactivateDailyRule() {
		PeriodicChangeRule periodicChangeRule = createPeriodicChangeRule(DAILY, LocalDate.of(2026, 10, 13));
		mock(periodicChangeRule);
		ArgumentCaptor<PeriodicChangeRule> argumentCaptor = ArgumentCaptor.forClass(PeriodicChangeRule.class);
		ruleExecuteService.ruleExecute(LocalDate.of(2026, 1, 13));
		verify(periodicChangeRuleRepository).save(argumentCaptor.capture());
		PeriodicChangeRule argumentCaptorValue = argumentCaptor.getValue();
		assertTrue(argumentCaptorValue.isActive());
		assertEquals(LocalDate.of(2026, 1, 14), argumentCaptorValue.getNextDay());
		verify(accountService, times(2)).applyRule(any());
	}

	@Test
	public void shouldNotDeactivateDailyRule() {
		PeriodicChangeRule periodicChangeRule = createPeriodicChangeRule(DAILY, LocalDate.of(2026, 1, 13));
		mock(periodicChangeRule);
		ArgumentCaptor<PeriodicChangeRule> argumentCaptor = ArgumentCaptor.forClass(PeriodicChangeRule.class);
		ruleExecuteService.ruleExecute(LocalDate.of(2026, 1, 13));
		verify(periodicChangeRuleRepository).save(argumentCaptor.capture());
		PeriodicChangeRule argumentCaptorValue = argumentCaptor.getValue();
		assertFalse(argumentCaptorValue.isActive());
		assertEquals(LocalDate.of(2026, 1, 14), argumentCaptorValue.getNextDay());
		verify(accountService, times(2)).applyRule(any());
	}

	@Test
	public void shouldDeactivateYearlyRule() {
		PeriodicChangeRule periodicChangeRule = createPeriodicChangeRule(YEARLY, LocalDate.of(2026, 1, 14));
		mock(periodicChangeRule);
		ArgumentCaptor<PeriodicChangeRule> argumentCaptor = ArgumentCaptor.forClass(PeriodicChangeRule.class);
		ruleExecuteService.ruleExecute(LocalDate.of(2026, 1, 13));
		verify(periodicChangeRuleRepository).save(argumentCaptor.capture());
		PeriodicChangeRule argumentCaptorValue = argumentCaptor.getValue();
		assertFalse(argumentCaptorValue.isActive());
		assertEquals(LocalDate.of(2027, 1, 12), argumentCaptorValue.getNextDay());
		verify(accountService, times(1)).applyRule(any());
	}

	@Test
	public void shouldNotDeactivateYearlyRule() {
		PeriodicChangeRule periodicChangeRule = createPeriodicChangeRule(YEARLY, LocalDate.of(2028, 1, 14));
		mock(periodicChangeRule);
		ArgumentCaptor<PeriodicChangeRule> argumentCaptor = ArgumentCaptor.forClass(PeriodicChangeRule.class);
		ruleExecuteService.ruleExecute(LocalDate.of(2026, 1, 13));
		verify(periodicChangeRuleRepository).save(argumentCaptor.capture());
		PeriodicChangeRule argumentCaptorValue = argumentCaptor.getValue();
		assertTrue(argumentCaptorValue.isActive());
		assertEquals(LocalDate.of(2027, 1, 12), argumentCaptorValue.getNextDay());
		verify(accountService, times(1)).applyRule(any());
	}

	@Test
	public void shouldDeactivateOnceRule() {
		PeriodicChangeRule periodicChangeRule = createPeriodicChangeRule(ONE, null);
		mock(periodicChangeRule);
		ArgumentCaptor<PeriodicChangeRule> argumentCaptor = ArgumentCaptor.forClass(PeriodicChangeRule.class);
		ruleExecuteService.ruleExecute(LocalDate.of(2026, 1, 13));
		verify(periodicChangeRuleRepository).save(argumentCaptor.capture());
		PeriodicChangeRule argumentCaptorValue = argumentCaptor.getValue();
		assertFalse(argumentCaptorValue.isActive());
		assertEquals(LocalDate.of(2026, 1, 12), argumentCaptorValue.getNextDay());
		verify(accountService, times(1)).applyRule(any());
	}

	@Test
	public void shouldDeactivateWeeklyRule() {
		PeriodicChangeRule periodicChangeRule = createPeriodicChangeRule(WEEKLY, LocalDate.of(2026, 1, 14));
		mock(periodicChangeRule);
		ArgumentCaptor<PeriodicChangeRule> argumentCaptor = ArgumentCaptor.forClass(PeriodicChangeRule.class);
		ruleExecuteService.ruleExecute(LocalDate.of(2026, 1, 13));
		verify(periodicChangeRuleRepository).save(argumentCaptor.capture());
		PeriodicChangeRule argumentCaptorValue = argumentCaptor.getValue();
		assertFalse(argumentCaptorValue.isActive());
		assertEquals(LocalDate.of(2026, 1, 19), argumentCaptorValue.getNextDay());
		verify(accountService, times(1)).applyRule(any());
	}

	@Test
	public void shouldNotDeactivateWeeklyRule() {
		PeriodicChangeRule periodicChangeRule = createPeriodicChangeRule(WEEKLY, LocalDate.of(2026, 2, 14));
		mock(periodicChangeRule);
		ArgumentCaptor<PeriodicChangeRule> argumentCaptor = ArgumentCaptor.forClass(PeriodicChangeRule.class);
		ruleExecuteService.ruleExecute(LocalDate.of(2026, 1, 13));
		verify(periodicChangeRuleRepository).save(argumentCaptor.capture());
		PeriodicChangeRule argumentCaptorValue = argumentCaptor.getValue();
		assertTrue(argumentCaptorValue.isActive());
		assertEquals(LocalDate.of(2026, 1, 19), argumentCaptorValue.getNextDay());
		verify(accountService, times(1)).applyRule(any());
	}

	private PeriodicChangeRule createPeriodicChangeRule(Type type, LocalDate endDate) {
		return new PeriodicChangeRule(1L, 1L, 1L,
				"Платеж", -3000, type, 0,
				LocalDate.of(2026, 1, 12), true, endDate);
	}

	private void mock(PeriodicChangeRule periodicChangeRule) {
		when(periodicChangeRepository.findAll()).thenReturn(
				List.of(new PeriodicChange(1L, "Безопасное место для денег", 1L, Set.of(periodicChangeRule)))
		);
		when(periodicChangeRuleRepository.findByActiveTrueAndNextDayLessThanEqual(any())).thenReturn(List.of(periodicChangeRule));
		when(accountService.applyRule(any(PeriodicChangeRule.class))).thenReturn(new ApplyResult(120, 150));
	}
}