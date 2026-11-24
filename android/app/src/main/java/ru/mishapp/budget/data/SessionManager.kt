package ru.mishapp.budget.data

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.mishapp.budget.data.local.TokenStore
import ru.mishapp.budget.data.local.Tokens
import ru.mishapp.budget.data.remote.BudgetApi
import ru.mishapp.budget.data.remote.LoginRequestDto
import ru.mishapp.budget.data.remote.RefreshTokenRequestDto
import ru.mishapp.budget.domain.AuthUser

sealed interface AuthState {
    data object Loading : AuthState
    data object LoggedOut : AuthState
    data class LoggedIn(val user: AuthUser) : AuthState
}

@Singleton
class SessionManager @Inject constructor(
    private val tokenStore: TokenStore,
    private val api: BudgetApi,
) {

    private val _state = MutableStateFlow<AuthState>(AuthState.Loading)
    val state: StateFlow<AuthState> = _state.asStateFlow()

    private var currentUser: AuthUser? = null

    /** Проверяет сохранённую сессию при старте приложения. */
    suspend fun bootstrap() {
        val tokens = tokenStore.load()
        if (tokens == null) {
            _state.value = AuthState.LoggedOut
            return
        }
        try {
            val user = apiCall { api.me() }
            currentUser = AuthUser(user.id, user.login)
            _state.value = AuthState.LoggedIn(currentUser!!)
        } catch (exception: ApiException) {
            if (exception.unauthorized) {
                tokenStore.clear()
            }
            _state.value = AuthState.LoggedOut
        }
    }

    suspend fun login(login: String, password: String) {
        val tokens = apiCall { api.login(LoginRequestDto(login.trim(), password)) }
        tokenStore.save(Tokens(tokens.accessToken, tokens.refreshToken))
        val user = apiCall { api.me() }
        currentUser = AuthUser(user.id, user.login)
        _state.value = AuthState.LoggedIn(currentUser!!)
    }

    suspend fun logout() {
        val refresh = tokenStore.currentRefreshToken()
        if (refresh != null) {
            runCatching { api.logout(RefreshTokenRequestDto(refresh)) }
        }
        tokenStore.clear()
        forceLogout()
    }

    /** Вызывается, когда обновить токен не удалось. */
    fun forceLogout() {
        currentUser = null
        _state.value = AuthState.LoggedOut
    }
}
