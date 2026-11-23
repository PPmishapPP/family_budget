package ru.mishapp.budget.data

import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.Interceptor
import okhttp3.Response
import ru.mishapp.budget.data.local.TokenStore

/** Подставляет access-токен во все запросы, кроме ручек авторизации. */
@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenStore: TokenStore,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        // Токен не нужен только для самих ручек входа: login/refresh/logout.
        // Внимание: `/api/v1/auth/me` — защищённая ручка, её нельзя пропускать.
        if (NO_AUTH_PATHS.any { request.url.encodedPath.endsWith(it) }) {
            return chain.proceed(request)
        }
        val token = tokenStore.currentAccessToken()
            ?: return chain.proceed(request)
        return chain.proceed(
            request.newBuilder()
                .header("Authorization", "Bearer $token")
                .build(),
        )
    }

    private companion object {
        val NO_AUTH_PATHS = listOf(
            "/api/v1/auth/login",
            "/api/v1/auth/refresh",
            "/api/v1/auth/logout",
        )
    }
}
