package ru.mishapp.budget.util

import java.text.NumberFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

private val ruLocale: Locale = Locale("ru", "RU")

private val rubFormat: NumberFormat = NumberFormat.getIntegerInstance(ruLocale)

private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", ruLocale)

private val monthYearFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("LLLL yyyy", ruLocale)

private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", ruLocale)

/** Сумма со знаком и символом рубля: `−1 250 ₽`, `+300 ₽`. */
fun formatMoney(value: Int, signed: Boolean = false): String {
    val prefix = when {
        signed && value > 0 -> "+"
        value < 0 -> "−"
        else -> ""
    }
    return "$prefix${rubFormat.format(kotlin.math.abs(value.toLong()))}\u00A0₽"
}

/** Сумма без знака, для баланса. */
fun formatBalance(value: Int): String = "${rubFormat.format(value.toLong())}\u00A0₽"

fun formatSignedBalance(value: Int): String {
    val prefix = if (value < 0) "−" else ""
    return "$prefix${rubFormat.format(kotlin.math.abs(value.toLong()))}\u00A0₽"
}

fun formatDate(date: LocalDate?): String = date?.format(dateFormatter) ?: "—"

fun formatMonthYear(date: LocalDate): String = date.format(monthYearFormatter)
    .replaceFirstChar { it.titlecase(ruLocale) }

fun formatDateTime(value: LocalDateTime?): String = value?.format(dateTimeFormatter) ?: "—"

/** Полное имя даты для деталей, например «пн, 1 декабря 2026». */
fun formatFullDate(date: LocalDate): String =
    date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(ruLocale))

fun pluralize(count: Int, one: String, few: String, many: String): String {
    val mod100 = count % 100
    val mod10 = count % 10
    return when {
        mod100 in 11..14 -> many
        mod10 == 1 -> one
        mod10 in 2..4 -> few
        else -> many
    }
}
