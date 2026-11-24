package ru.mishapp.budget.ui.forecast

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
import ru.mishapp.budget.domain.ForecastItem
import ru.mishapp.budget.domain.IncomeForecastItem
import java.time.LocalDate

enum class ForecastTab(val label: String) { PAYMENTS("Платежи"), INCOME("Доход") }

/** Первое число месяца через [monthsAhead] месяцев; используется быстрыми кнопками. */
fun firstOfMonth(monthsAhead: Long): LocalDate =
    LocalDate.now().plusMonths(monthsAhead).withDayOfMonth(1)

val minForecastDate: LocalDate get() = LocalDate.now().plusDays(1)
val maxForecastDate: LocalDate get() = LocalDate.now().plusYears(3)

data class ForecastUiState(
    val tab: ForecastTab = ForecastTab.PAYMENTS,
    val toDate: LocalDate = firstOfMonth(1),
    val loading: Boolean = false,
    val payments: List<ForecastItem>? = null,
    val income: List<IncomeForecastItem>? = null,
    val error: String? = null,
    /** Есть ли у счёта правила; `null` — ещё не известно (C-20). */
    val hasRules: Boolean? = null,
) {
    val dateValid: Boolean get() = !toDate.isBefore(minForecastDate) && !toDate.isAfter(maxForecastDate)

    /** Как в web (C-20): без правил расчёт недоступен. */
    val canCalculate: Boolean get() = dateValid && !loading && hasRules != false
}

@HiltViewModel
class ForecastViewModel @Inject constructor(
    private val repository: BudgetRepository,
) : ViewModel() {

    private var accountId: Long? = null

    private val _uiState = MutableStateFlow(ForecastUiState())
    val uiState: StateFlow<ForecastUiState> = _uiState.asStateFlow()

    fun attach(id: Long) {
        if (accountId != id) {
            accountId = id
            _uiState.update { it.copy(hasRules = null, payments = null, income = null, error = null) }
        }
        refresh()
    }

    /** Перепроверяет доступность расчёта и пересчитывает прогноз (C-20). */
    fun refresh() {
        val id = accountId ?: return
        viewModelScope.launch {
            val hasRules = try {
                repository.rules(id).isNotEmpty()
            } catch (exception: ApiException) {
                // Ошибка вспомогательного запроса не должна блокировать расчёт.
                null
            }
            _uiState.update { it.copy(hasRules = hasRules) }
            // Как в web (C-20): без правил прогноз не запускаем.
            if (hasRules != false) calculate()
        }
    }

    fun setTab(tab: ForecastTab) = _uiState.update { it.copy(tab = tab) }

    fun setDate(date: LocalDate) = _uiState.update { it.copy(toDate = date) }

    fun calculate() {
        val id = accountId ?: return
        val state = _uiState.value
        if (state.hasRules == false) {
            // C-20: без правил расчёт недоступен, как в web.
            return
        }
        if (!state.dateValid) {
            _uiState.update {
                it.copy(error = "Выберите дату в пределах от завтрашнего дня до +3 лет")
            }
            return
        }
        _uiState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                if (state.tab == ForecastTab.PAYMENTS) {
                    val items = repository.forecast(id, state.toDate, rules = null)
                    _uiState.update { it.copy(loading = false, payments = items) }
                } else {
                    val items = repository.incomeForecast(id, state.toDate)
                    _uiState.update { it.copy(loading = false, income = items) }
                }
            } catch (exception: ApiException) {
                _uiState.update { it.copy(loading = false, error = exception.message) }
            }
        }
    }

    fun clear() = _uiState.update { it.copy(payments = null, income = null, error = null) }
}
