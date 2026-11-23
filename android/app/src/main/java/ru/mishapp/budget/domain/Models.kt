package ru.mishapp.budget.domain

import java.time.LocalDate
import java.time.LocalDateTime

/** Тип периодического правила. Значения совпадают с API. */
enum class RuleType(val apiValue: String, val label: String) {
    MONTHLY("MONTHLY", "Ежемесячно"),
    WEEKLY("WEEKLY", "Еженедельно"),
    DAILY("DAILY", "Ежедневно"),
    ONE("ONE", "Единоразово"),
    YEARLY("YEARLY", "Ежегодно"),
    ;

    companion object {
        fun fromApi(value: String?): RuleType? = entries.firstOrNull { it.apiValue == value }
    }
}

data class PaymentRule(
    val id: Long,
    val name: String,
    val sum: Int,
    val type: RuleType,
    val pass: Int,
    val nextDay: LocalDate,
    val endDate: LocalDate?,
) {
    val isDue: Boolean get() = !nextDay.isAfter(LocalDate.now())
}

data class RuleInput(
    val name: String,
    val sum: Int,
    val type: RuleType,
    val nextDay: LocalDate,
    val endDate: LocalDate?,
)

enum class PaymentDirection(val apiValue: String, val label: String) {
    EXPENSE("EXPENSE", "Расход"),
    INCOME("INCOME", "Доход"),
}

data class HistoryEntry(
    val id: Long,
    val sum: Int,
    val balance: Int,
    val dateTime: LocalDateTime,
    val comment: String?,
)

data class HistoryPage(
    val entries: List<HistoryEntry>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
)

data class MediaItem(
    val id: Long,
    val name: String,
    val type: String,
    val status: String,
    val rating: Int?,
    val description: String?,
)

data class MediaInput(
    val name: String,
    val type: String,
    val status: String?,
    val rating: Int?,
    val description: String?,
)

data class ForecastItem(
    val date: LocalDate?,
    val operation: String?,
    val sum: Int,
    val balance: Int,
)

data class IncomeForecastItem(
    val date: LocalDate?,
    val balance: Int,
    val increase: Int?,
)

data class Reference(val value: String, val label: String)

data class MediaStatus(
    val value: String,
    val label: String,
    val availableTransitions: List<String>,
)

data class Account(val id: Long, val name: String, val active: Boolean)

data class AuthUser(val id: Long, val login: String)
