package ru.mishapp.budget.ui.main

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
import ru.mishapp.budget.data.BudgetRepository
import ru.mishapp.budget.data.SessionManager
import ru.mishapp.budget.domain.Account

data class MainUiState(
    val loading: Boolean = true,
    val accounts: List<Account> = emptyList(),
    val currentAccountId: Long? = null,
    val error: String? = null,
) {
    val currentAccount: Account? get() = accounts.firstOrNull { it.id == currentAccountId }
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: BudgetRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        loadAccounts()
    }

    fun loadAccounts() {
        _uiState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                val accounts = repository.accounts()
                _uiState.update { state ->
                    val selected = state.currentAccountId?.takeIf { id -> accounts.any { it.id == id } }
                        ?: accounts.firstOrNull()?.id
                    state.copy(loading = false, accounts = accounts, currentAccountId = selected)
                }
            } catch (exception: ApiException) {
                _uiState.update { it.copy(loading = false, error = exception.message) }
            }
        }
    }

    fun selectAccount(accountId: Long) {
        _uiState.update { it.copy(currentAccountId = accountId) }
    }

    fun logout() {
        viewModelScope.launch { sessionManager.logout() }
    }
}
