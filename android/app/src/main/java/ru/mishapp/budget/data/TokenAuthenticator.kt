package ru.mishapp.budget.data

import dagger.Lazy
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import ru.mishapp.budget.data.local.TokenStore
import ru.mishapp.budget.data.local.Tokens
import ru.mishapp.budget.data.remote.BudgetApi
import ru.mishapp.budget.data.remote.RefreshTokenRequestDto

/**
 * Обновляет access-токен при 401 (single-flight: параллельные запросы ждут один refresh).
 * Если refresh не удался — чистит сессию, приложение вернётся на экран входа.
 */
@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenStore: TokenStore,
    private val authApi: Lazy<BudgetApi>,
    private val sessionManager: Lazy<SessionManager>,
) : Authenticator {

    private val mutex = Mutex()

    override fun authenticate(route: Route?, response: Response): Request? {
        if (isAuthEndpoint(response.request)) return null
        if (responseCount(response) >= 2) return null

        val failedToken = response.request.header("Authorization")?.removePrefix("Bearer ")

        return runBlocking {
            mutex.withLock {
                val current = tokenStore.currentAccessToken()
                if (current != null && current != failedToken) {
                    return@withLock response.request
                        .newBuilder()
                        .header("Authorization", "Bearer $current")
                        .build()
                }

                val refreshToken = tokenStore.currentRefreshToken()
                if (refreshToken == null) {
                    sessionManager.get().forceLogout()
                    return@withLock null
                }

                try {
                    val tokens = authApi.get().refresh(RefreshTokenRequestDto(refreshToken))
                    tokenStore.save(Tokens(tokens.accessToken, tokens.refreshToken))
                    response.request
                        .newBuilder()
                        .header("Authorization", "Bearer ${tokens.accessToken}")
                        .build()
                } catch (exception: Exception) {
                    tokenStore.clear()
                    sessionManager.get().forceLogout()
                    null
                }
            }
        }
    }

    private fun isAuthEndpoint(request: Request): Boolean =
        NO_AUTH_PATHS.any { request.url.encodedPath.endsWith(it) }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }

    private companion object {
        val NO_AUTH_PATHS = listOf(
            "/api/v1/auth/login",
            "/api/v1/auth/refresh",
            "/api/v1/auth/logout",
        )
    }
}
