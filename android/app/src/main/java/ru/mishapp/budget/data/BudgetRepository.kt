package ru.mishapp.budget.data

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton
import ru.mishapp.budget.data.remote.BudgetApi
import ru.mishapp.budget.data.remote.ForecastRequestDto
import ru.mishapp.budget.data.remote.IncomeForecastRequestDto
import ru.mishapp.budget.data.remote.InstantPaymentRequestDto
import ru.mishapp.budget.data.remote.MediaRequestDto
import ru.mishapp.budget.data.remote.PaymentRuleRequestDto
import ru.mishapp.budget.domain.Account
import ru.mishapp.budget.domain.ForecastItem
import ru.mishapp.budget.domain.HistoryEntry
import ru.mishapp.budget.domain.HistoryPage
import ru.mishapp.budget.domain.IncomeForecastItem
import ru.mishapp.budget.domain.MediaInput
import ru.mishapp.budget.domain.MediaItem
import ru.mishapp.budget.domain.MediaStatus
import ru.mishapp.budget.domain.PaymentDirection
import ru.mishapp.budget.domain.PaymentRule
import ru.mishapp.budget.domain.Reference
import ru.mishapp.budget.domain.RuleInput
import ru.mishapp.budget.domain.RuleType

@Singleton
class BudgetRepository @Inject constructor(
    private val api: BudgetApi,
) {

    suspend fun accounts(): List<Account> = apiCall {
        api.accounts().map { Account(it.id, it.name, it.active) }
    }

    suspend fun balance(accountId: Long): Int = apiCall { api.balance(accountId).balance }

    // --- Правила платежей ---

    suspend fun rules(accountId: Long): List<PaymentRule> = apiCall {
        api.paymentRules(accountId).mapNotNull { dto ->
            val type = RuleType.fromApi(dto.type) ?: return@mapNotNull null
            val nextDay = dto.nextDay?.let(::parseDate) ?: return@mapNotNull null
            PaymentRule(
                id = dto.id,
                name = dto.name,
                sum = dto.sum,
                type = type,
                pass = dto.pass,
                nextDay = nextDay,
                endDate = dto.endDate?.let(::parseDate),
            )
        }.sortedBy { it.nextDay }
    }

    suspend fun createRule(accountId: Long, input: RuleInput): PaymentRule = apiCall {
        api.createRule(accountId, input.toRequest()).toDomain()!!
    }

    suspend fun updateRule(accountId: Long, ruleId: Long, input: RuleInput): PaymentRule = apiCall {
        api.updateRule(accountId, ruleId, input.toRequest()).toDomain()!!
    }

    suspend fun deleteRule(accountId: Long, ruleId: Long) = apiCall {
        requireSuccess(api.deleteRule(accountId, ruleId))
    }

    suspend fun executeRule(accountId: Long, ruleId: Long) = apiCall {
        requireSuccess(api.executeRule(accountId, ruleId))
    }

    suspend fun registerPayment(
        accountId: Long,
        name: String,
        amount: Int,
        direction: PaymentDirection,
    ): HistoryEntry = apiCall {
        val dto = api.registerPayment(
            accountId,
            InstantPaymentRequestDto(name = name, amount = amount, direction = direction.apiValue),
        )
        HistoryEntry(
            id = dto.id,
            sum = dto.sum,
            balance = dto.balance,
            dateTime = dto.dateTime?.let(::parseDateTime) ?: LocalDateTime.now(),
            comment = dto.comment,
        )
    }

    // --- История ---

    suspend fun history(
        accountId: Long,
        page: Int,
        size: Int,
        sort: String,
        direction: String,
    ): HistoryPage = apiCall {
        val dto = api.history(accountId, page, size, sort, direction)
        HistoryPage(
            entries = dto.content.mapNotNull { entry ->
                val dateTime = entry.dateTime?.let(::parseDateTime) ?: return@mapNotNull null
                HistoryEntry(
                    id = entry.id,
                    sum = entry.sum,
                    balance = entry.balance,
                    dateTime = dateTime,
                    comment = entry.comment,
                )
            },
            page = dto.page,
            size = dto.size,
            totalElements = dto.totalElements,
            totalPages = dto.totalPages,
        )
    }

    // --- Прогнозы ---

    suspend fun forecast(accountId: Long, to: LocalDate, rules: List<PaymentRule>?): List<ForecastItem> =
        apiCall {
            val request = ForecastRequestDto(
                to = to.toString(),
                rules = rules?.map { it.toRequest() },
            )
            api.forecast(accountId, request).map {
                ForecastItem(
                    date = it.date?.let(::parseDate),
                    operation = it.operation,
                    sum = it.sum,
                    balance = it.balance,
                )
            }
        }

    suspend fun incomeForecast(accountId: Long, to: LocalDate): List<IncomeForecastItem> = apiCall {
        api.incomeForecast(accountId, IncomeForecastRequestDto(to.toString())).map {
            IncomeForecastItem(
                date = it.date?.let(::parseDate),
                balance = it.balance,
                increase = it.increase,
            )
        }
    }

    // --- Медиа ---

    suspend fun media(accountId: Long, type: String?): List<MediaItem> = apiCall {
        api.media(accountId, type).map {
            MediaItem(
                id = it.id,
                name = it.name,
                type = it.type ?: "MOVIE",
                status = it.status ?: "PLAN_TO_WATCH",
                rating = it.rating,
                description = it.description,
            )
        }
    }

    suspend fun createMedia(accountId: Long, input: MediaInput): MediaItem = apiCall {
        api.createMedia(accountId, input.toRequest()).toDomain()
    }

    suspend fun updateMedia(accountId: Long, mediaId: Long, input: MediaInput): MediaItem = apiCall {
        api.updateMedia(accountId, mediaId, input.toRequest()).toDomain()
    }

    suspend fun deleteMedia(accountId: Long, mediaId: Long) = apiCall {
        requireSuccess(api.deleteMedia(accountId, mediaId))
    }

    suspend fun mediaTypes(): List<Reference> = apiCall {
        api.mediaTypes().map { Reference(it.value, it.label) }
    }

    suspend fun mediaStatuses(): List<MediaStatus> = apiCall {
        api.mediaStatuses().map { MediaStatus(it.value, it.label, it.availableTransitions) }
    }
}

private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

private fun parseDate(value: String): LocalDate? = runCatching { LocalDate.parse(value, dateFormatter) }.getOrNull()

private fun parseDateTime(value: String): LocalDateTime? =
    runCatching { LocalDateTime.parse(value.take(19)) }.getOrNull()

private fun RuleInput.toRequest() = PaymentRuleRequestDto(
    name = name,
    sum = sum,
    type = type.apiValue,
    nextDay = nextDay.toString(),
    endDate = endDate?.toString(),
)

private fun PaymentRule.toRequest() = PaymentRuleRequestDto(
    name = name,
    sum = sum,
    type = type.apiValue,
    nextDay = nextDay.toString(),
    endDate = endDate?.toString(),
)

private fun ru.mishapp.budget.data.remote.PaymentRuleDto.toDomain(): PaymentRule? {
    val ruleType = RuleType.fromApi(type) ?: return null
    val due = nextDay?.let(::parseDate) ?: return null
    return PaymentRule(
        id = id,
        name = name,
        sum = sum,
        type = ruleType,
        pass = pass,
        nextDay = due,
        endDate = endDate?.let(::parseDate),
    )
}

private fun MediaInput.toRequest() = MediaRequestDto(
    name = name,
    type = type,
    status = status,
    rating = rating,
    description = description,
)

private fun ru.mishapp.budget.data.remote.MediaDto.toDomain() = MediaItem(
    id = id,
    name = name,
    type = type ?: "MOVIE",
    status = status ?: "PLAN_TO_WATCH",
    rating = rating,
    description = description,
)
