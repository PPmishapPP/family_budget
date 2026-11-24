package ru.mishapp.budget.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.mishapp.budget.data.ApiException
import ru.mishapp.budget.data.SessionManager

data class LoginUiState(
    val login: String = "",
    val password: String = "",
    val loading: Boolean = false,
    val error: String? = null,
) {
    val canSubmit: Boolean get() = login.isNotBlank() && password.isNotBlank() && !loading
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onLoginChange(value: String) = _uiState.update { it.copy(login = value, error = null) }

    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, error = null) }

    fun submit() {
        val state = _uiState.value
        if (!state.canSubmit) return
        _uiState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                sessionManager.login(state.login, state.password)
            } catch (exception: ApiException) {
                _uiState.update { it.copy(loading = false, error = exception.message) }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(loading = false, error = exception.message ?: "Не удалось войти")
                }
            }
        }
    }
}
