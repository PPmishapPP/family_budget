package ru.mishapp.budget.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.firstOrNull

private val Context.tokenDataStore: DataStore<Preferences> by preferencesDataStore(name = "auth")

data class Tokens(val accessToken: String, val refreshToken: String)

/**
 * Хранит пару токенов в DataStore (зашифрованные) и дублирует их в памяти,
 * чтобы OkHttp-перехватчик мог обращаться синхронно.
 */
@Singleton
class TokenStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    @Volatile
    private var cached: Tokens? = null

    /** Синхронный доступ для перехватчиков; наполняется при [load]/[save]. */
    fun currentAccessToken(): String? = cached?.accessToken

    fun currentRefreshToken(): String? = cached?.refreshToken

    suspend fun load(): Tokens? {
        val prefs = context.tokenDataStore.data.firstOrNull()
        val access = prefs?.get(ACCESS)?.let(Crypto::decrypt)
        val refresh = prefs?.get(REFRESH)?.let(Crypto::decrypt)
        cached = if (access != null && refresh != null) Tokens(access, refresh) else null
        return cached
    }

    suspend fun save(tokens: Tokens) {
        cached = tokens
        context.tokenDataStore.edit { prefs ->
            prefs[ACCESS] = Crypto.encrypt(tokens.accessToken)
            prefs[REFRESH] = Crypto.encrypt(tokens.refreshToken)
        }
    }

    suspend fun clear() {
        cached = null
        context.tokenDataStore.edit { it.clear() }
    }

    private companion object {
        val ACCESS = stringPreferencesKey("access_token")
        val REFRESH = stringPreferencesKey("refresh_token")
    }
}
