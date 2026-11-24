package ru.mishapp.budget.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/** Полный REST-контракт backend (`/api/v1`). */
interface BudgetApi {

    @POST("api/v1/auth/login")
    suspend fun login(@Body body: LoginRequestDto): TokenResponseDto

    @POST("api/v1/auth/refresh")
    suspend fun refresh(@Body body: RefreshTokenRequestDto): TokenResponseDto

    @POST("api/v1/auth/logout")
    suspend fun logout(@Body body: RefreshTokenRequestDto): Response<Unit>

    @GET("api/v1/auth/me")
    suspend fun me(): UserDto

    @GET("api/v1/accounts")
    suspend fun accounts(): List<AccountDto>

    @GET("api/v1/accounts/{accountId}/balance")
    suspend fun balance(@Path("accountId") accountId: Long): BalanceDto

    // --- Правила платежей ---

    @GET("api/v1/accounts/{accountId}/payment-rules")
    suspend fun paymentRules(@Path("accountId") accountId: Long): List<PaymentRuleDto>

    @POST("api/v1/accounts/{accountId}/payment-rules")
    suspend fun createRule(
        @Path("accountId") accountId: Long,
        @Body body: PaymentRuleRequestDto,
    ): PaymentRuleDto

    @PUT("api/v1/accounts/{accountId}/payment-rules/{ruleId}")
    suspend fun updateRule(
        @Path("accountId") accountId: Long,
        @Path("ruleId") ruleId: Long,
        @Body body: PaymentRuleRequestDto,
    ): PaymentRuleDto

    @DELETE("api/v1/accounts/{accountId}/payment-rules/{ruleId}")
    suspend fun deleteRule(
        @Path("accountId") accountId: Long,
        @Path("ruleId") ruleId: Long,
    ): Response<Unit>

    @POST("api/v1/accounts/{accountId}/payment-rules/{ruleId}/execute")
    suspend fun executeRule(
        @Path("accountId") accountId: Long,
        @Path("ruleId") ruleId: Long,
    ): Response<Unit>

    // --- Разовые платежи и история ---

    @POST("api/v1/accounts/{accountId}/payments")
    suspend fun registerPayment(
        @Path("accountId") accountId: Long,
        @Body body: InstantPaymentRequestDto,
    ): AccountHistoryDto

    @GET("api/v1/accounts/{accountId}/history")
    suspend fun history(
        @Path("accountId") accountId: Long,
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("sort") sort: String,
        @Query("direction") direction: String,
    ): PageResponseAccountHistoryDto

    // --- Прогнозы ---

    @POST("api/v1/accounts/{accountId}/forecast")
    suspend fun forecast(
        @Path("accountId") accountId: Long,
        @Body body: ForecastRequestDto,
    ): List<ForecastItemDto>

    @POST("api/v1/accounts/{accountId}/income-forecast")
    suspend fun incomeForecast(
        @Path("accountId") accountId: Long,
        @Body body: IncomeForecastRequestDto,
    ): List<IncomeForecastItemDto>

    // --- Медиа ---

    @GET("api/v1/accounts/{accountId}/media")
    suspend fun media(
        @Path("accountId") accountId: Long,
        @Query("type") type: String? = null,
    ): List<MediaDto>

    @POST("api/v1/accounts/{accountId}/media")
    suspend fun createMedia(
        @Path("accountId") accountId: Long,
        @Body body: MediaRequestDto,
    ): MediaDto

    @PUT("api/v1/accounts/{accountId}/media/{mediaId}")
    suspend fun updateMedia(
        @Path("accountId") accountId: Long,
        @Path("mediaId") mediaId: Long,
        @Body body: MediaRequestDto,
    ): MediaDto

    @DELETE("api/v1/accounts/{accountId}/media/{mediaId}")
    suspend fun deleteMedia(
        @Path("accountId") accountId: Long,
        @Path("mediaId") mediaId: Long,
    ): Response<Unit>

    // --- Справочники ---

    @GET("api/v1/reference/payment-types")
    suspend fun paymentTypes(): List<ReferenceDto>

    @GET("api/v1/reference/media-types")
    suspend fun mediaTypes(): List<ReferenceDto>

    @GET("api/v1/reference/media-statuses")
    suspend fun mediaStatuses(): List<MediaStatusDto>
}
