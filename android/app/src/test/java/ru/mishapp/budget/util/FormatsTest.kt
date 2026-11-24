package ru.mishapp.budget.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class FormatsTest {

    @Test
    fun `money without sign has no prefix`() {
        assertEquals("1\u00A0250\u00A0₽", formatMoney(1250))
    }

    @Test
    fun `signed positive gets plus`() {
        assertEquals("+300\u00A0₽", formatMoney(300, signed = true))
    }

    @Test
    fun `negative always gets minus`() {
        assertEquals("−1\u00A0250\u00A0₽", formatMoney(-1250))
    }

    @Test
    fun `signed balance negative`() {
        assertEquals("−500\u00A0₽", formatSignedBalance(-500))
    }

    @Test
    fun `date formats as dd dot mm dot yyyy`() {
        assertEquals("01.12.2026", formatDate(LocalDate.of(2026, 12, 1)))
    }

    @Test
    fun `null date renders dash`() {
        assertEquals("—", formatDate(null))
    }

    @Test
    fun `pluralize russian rules`() {
        assertEquals("платёж", pluralize(1, "платёж", "платежа", "платежей"))
        assertEquals("платежа", pluralize(3, "платёж", "платежа", "платежей"))
        assertEquals("платежей", pluralize(5, "платёж", "платежа", "платежей"))
        assertEquals("платежей", pluralize(11, "платёж", "платежа", "платежей"))
        assertEquals("платёж", pluralize(21, "платёж", "платежа", "платежей"))
    }

    @Test
    fun `month year is capitalized russian`() {
        assertEquals("Декабрь 2026", formatMonthYear(LocalDate.of(2026, 12, 15)))
    }
}
