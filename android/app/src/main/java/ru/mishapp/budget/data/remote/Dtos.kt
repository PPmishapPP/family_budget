package ru.mishapp.budget.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(val login: String, val password: String)

@Serializable
data class RefreshTokenRequestDto(val refreshToken: String)

@Serializable
data class TokenResponseDto(
    val accessToken: String = "",
    val refreshToken: String = "",
    val tokenType: String = "",
    val expiresIn: Long = 0,
)

@Serializable
data class UserDto(val id: Long = 0, val login: String = "")

@Serializable
data class AccountDto(val id: Long = 0, val name: String = "", val active: Boolean = false)

@Serializable
data class BalanceDto(val accountId: Long = 0, val balance: Int = 0)

@Serializable
data class PaymentRuleDto(
    val id: Long = 0,
    val name: String = "",
    val sum: Int = 0,
    val type: String = "",
    val pass: Int = 0,
    val nextDay: String? = null,
    val endDate: String? = null,
)

@Serializable
data class PaymentRuleRequestDto(
    val name: String,
    val sum: Int,
    val type: String,
    val nextDay: String,
    val endDate: String? = null,
)

@Serializable
data class InstantPaymentRequestDto(
    val name: String,
    val amount: Int,
    val direction: String,
)

@Serializable
data class AccountHistoryDto(
    val id: Long = 0,
    val sum: Int = 0,
    val balance: Int = 0,
    val dateTime: String? = null,
    val comment: String? = null,
)

@Serializable
data class PageResponseAccountHistoryDto(
    val content: List<AccountHistoryDto> = emptyList(),
    val page: Int = 0,
    val size: Int = 20,
    val totalElements: Long = 0,
    val totalPages: Int = 0,
)

@Serializable
data class MediaDto(
    val id: Long = 0,
    val name: String = "",
    val type: String? = null,
    val status: String? = null,
    val rating: Int? = null,
    val description: String? = null,
)

@Serializable
data class MediaRequestDto(
    val name: String,
    val type: String,
    val status: String? = null,
    val rating: Int? = null,
    val description: String? = null,
)

@Serializable
data class ReferenceDto(val value: String = "", val label: String = "")

@Serializable
data class MediaStatusDto(
    val value: String = "",
    val label: String = "",
    val availableTransitions: List<String> = emptyList(),
)

@Serializable
data class ForecastRequestDto(val to: String, val rules: List<PaymentRuleRequestDto>? = null)

@Serializable
data class ForecastItemDto(
    val date: String? = null,
    val operation: String? = null,
    val sum: Int = 0,
    val balance: Int = 0,
)

@Serializable
data class IncomeForecastRequestDto(val to: String)

@Serializable
data class IncomeForecastItemDto(
    val date: String? = null,
    val balance: Int = 0,
    val increase: Int? = null,
)

@Serializable
data class ProblemDto(
    val title: String? = null,
    val detail: String? = null,
    val status: Int? = null,
)
