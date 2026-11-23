package ru.mishapp.budget.ui.payments

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import ru.mishapp.budget.domain.PaymentRule
import ru.mishapp.budget.domain.PaymentDirection
import ru.mishapp.budget.domain.RuleType

class PaymentsSortingTest {

    private val today = LocalDate.now()

    private val rules = listOf(
        PaymentRule(1, "Яндекс", 200, RuleType.MONTHLY, 0, today.plusDays(5), null),
        PaymentRule(2, "Аренда", 30000, RuleType.MONTHLY, 0, today.minusDays(1), null),
        PaymentRule(3, "Spotify", 199, RuleType.MONTHLY, 0, today.plusDays(10), null),
        PaymentRule(4, "Интернет", 700, RuleType.MONTHLY, 0, today, null),
    )

    private fun state(sort: RuleSort, ascending: Boolean) =
        PaymentsUiState(loading = false, rules = rules, sort = sort, ascending = ascending)

    @Test
    fun `sort by sum ascending`() {
        val sums = state(RuleSort.SUM, true).visibleRules.map { it.sum }
        assertEquals(listOf(199, 200, 700, 30000), sums)
    }

    @Test
    fun `sort by sum descending`() {
        val sums = state(RuleSort.SUM, false).visibleRules.map { it.sum }
        assertEquals(listOf(30000, 700, 200, 199), sums)
    }

    @Test
    fun `sort by next date ascending`() {
        val dates = state(RuleSort.NEXT_DATE, true).visibleRules.map { it.nextDay }
        assertEquals(dates.sorted(), dates)
    }

    @Test
    fun `only due filters overdue and today`() {
        val due = state(RuleSort.NEXT_DATE, true).copy(onlyDue = true).visibleRules
        assertTrue(due.all { it.isDue })
        assertEquals(2, due.size)
    }

    @Test
    fun `due count counts overdue and today`() {
        assertEquals(2, state(RuleSort.NEXT_DATE, true).dueCount)
    }

    @Test
    fun `sort by name puts latin before cyrillic`() {
        // Реальная сортировка не зависит от регистра и совпадает с web (`localeCompare('ru')`):
        // латиница идёт раньше кириллицы, кириллица — по алфавиту.
        val names = state(RuleSort.NAME, true).visibleRules.map { it.name }
        assertEquals(listOf("Spotify", "Аренда", "Интернет", "Яндекс"), names)
    }

    @Test
    fun `sort by name ignores case`() {
        val mixed = listOf(
            PaymentRule(10, "apple", 1, RuleType.MONTHLY, 0, today, null),
            PaymentRule(11, "Banana", 1, RuleType.MONTHLY, 0, today, null),
            PaymentRule(12, "cherry", 1, RuleType.MONTHLY, 0, today, null),
        )
        val names = PaymentsUiState(loading = false, rules = mixed, sort = RuleSort.NAME, ascending = true)
            .visibleRules.map { it.name }
        assertEquals(listOf("apple", "Banana", "cherry"), names)
    }

    @Test
    fun `sort by end date puts open-ended last`() {
        val withEnd = listOf(
            PaymentRule(20, "Без даты", 1, RuleType.MONTHLY, 0, today, null),
            PaymentRule(21, "Позже", 1, RuleType.MONTHLY, 0, today, today.plusMonths(3)),
            PaymentRule(22, "Раньше", 1, RuleType.MONTHLY, 0, today, today.plusMonths(1)),
        )
        val names = PaymentsUiState(loading = false, rules = withEnd, sort = RuleSort.END_DATE, ascending = true)
            .visibleRules.map { it.name }
        assertEquals(listOf("Раньше", "Позже", "Без даты"), names)
    }

    @Test
    fun `expense rule saves as negative sum`() {
        assertEquals(-2500, signedRuleSum(PaymentDirection.EXPENSE, 2500))
    }

    @Test
    fun `income rule saves as positive sum`() {
        assertEquals(2500, signedRuleSum(PaymentDirection.INCOME, 2500))
    }
}
