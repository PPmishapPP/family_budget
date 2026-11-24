package ru.mishapp.budget.ui.payments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.mishapp.budget.data.ApiException
import ru.mishapp.budget.data.BudgetRepository
import ru.mishapp.budget.domain.PaymentDirection
import ru.mishapp.budget.domain.PaymentRule
import ru.mishapp.budget.domain.RuleInput

enum class RuleSort(val label: String) {
    NEXT_DATE("По дате"),
    SUM("По сумме"),
    NAME("По названию"),
    TYPE("По типу"),
    END_DATE("По последнему"),
}

/**
 * Знаковая сумма правила для API: расход — отрицательная, доход — положительный
 * (как в web, `PaymentRuleFormModal.test.tsx`). В UI пользователь вводит модуль.
 */
fun signedRuleSum(direction: PaymentDirection, magnitude: Int): Int =
    if (direction == PaymentDirection.EXPENSE) -magnitude else magnitude

data class PaymentsUiState(
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val balance: Int = 0,
    val rules: List<PaymentRule> = emptyList(),
    val error: String? = null,
    val sort: RuleSort = RuleSort.NEXT_DATE,
    val ascending: Boolean = true,
    val onlyDue: Boolean = false,
    val busyRuleId: Long? = null,
    val message: String? = null,
    /** Идёт сохранение формы: кнопка блокируется от двойного нажатия (H-05). */
    val saving: Boolean = false,
    /** Ошибка сохранения формы, показывается внутри открытого листа (C-19). */
    val formError: String? = null,
) {
    val visibleRules: List<PaymentRule>
        get() {
            val filtered = if (onlyDue) rules.filter { it.isDue } else rules
            val comparator: Comparator<PaymentRule> = when (sort) {
                RuleSort.NEXT_DATE -> compareBy<PaymentRule> { it.nextDay }
                RuleSort.SUM -> compareBy<PaymentRule> { it.sum }
                RuleSort.NAME -> compareBy<PaymentRule, String>(String.CASE_INSENSITIVE_ORDER) { it.name }
                RuleSort.TYPE -> compareBy<PaymentRule> { it.type.ordinal }.thenBy { it.nextDay }
                RuleSort.END_DATE -> compareBy<PaymentRule> { it.endDate ?: LocalDate.MAX }.thenBy { it.nextDay }
            }
            val sorted = filtered.sortedWith(comparator)
            return if (ascending) sorted else sorted.reversed()
        }

    val dueCount: Int get() = rules.count { it.isDue }
}

@HiltViewModel
class PaymentsViewModel @Inject constructor(
    private val repository: BudgetRepository,
) : ViewModel() {

    private var attachedAccountId: Long? = null

    private val _uiState = MutableStateFlow(PaymentsUiState())
    val uiState: StateFlow<PaymentsUiState> = _uiState.asStateFlow()

    private var deletedRule: PaymentRule? = null

    fun attach(accountId: Long) {
        if (attachedAccountId == accountId) return
        attachedAccountId = accountId
        load()
    }

    fun load() {
        val id = attachedAccountId ?: return
        _uiState.update { it.copy(loading = it.rules.isEmpty(), refreshing = it.rules.isNotEmpty(), error = null) }
        viewModelScope.launch {
            try {
                val balance = repository.balance(id)
                val rules = repository.rules(id)
                _uiState.update { it.copy(loading = false, refreshing = false, balance = balance, rules = rules) }
            } catch (exception: ApiException) {
                _uiState.update { it.copy(loading = false, refreshing = false, error = exception.message) }
            }
        }
    }

    fun setSort(sort: RuleSort) {
        _uiState.update {
            if (it.sort == sort) it.copy(ascending = !it.ascending) else it.copy(sort = sort, ascending = true)
        }
    }

    fun toggleOnlyDue() = _uiState.update { it.copy(onlyDue = !it.onlyDue) }

    fun execute(rule: PaymentRule) {
        val id = attachedAccountId ?: return
        _uiState.update { it.copy(busyRuleId = rule.id) }
        viewModelScope.launch {
            try {
                repository.executeRule(id, rule.id)
                _uiState.update { it.copy(message = "Платёж «${rule.name}» проведён") }
                load()
            } catch (exception: ApiException) {
                _uiState.update { it.copy(message = exception.message) }
            } finally {
                _uiState.update { it.copy(busyRuleId = null) }
            }
        }
    }

    fun delete(rule: PaymentRule) {
        val id = attachedAccountId ?: return
        _uiState.update { it.copy(busyRuleId = rule.id) }
        viewModelScope.launch {
            try {
                repository.deleteRule(id, rule.id)
                deletedRule = rule
                _uiState.update { it.copy(message = "Правило «${rule.name}» удалено") }
                load()
            } catch (exception: ApiException) {
                _uiState.update { it.copy(message = exception.message) }
            } finally {
                _uiState.update { it.copy(busyRuleId = null) }
            }
        }
    }

    fun undoDelete() {
        val id = attachedAccountId ?: return
        val rule = deletedRule ?: return
        deletedRule = null
        viewModelScope.launch {
            try {
                repository.createRule(
                    id,
                    RuleInput(rule.name, rule.sum, rule.type, rule.nextDay, rule.endDate),
                )
                _uiState.update { it.copy(message = "Правило восстановлено") }
                load()
            } catch (exception: ApiException) {
                _uiState.update { it.copy(message = exception.message) }
            }
        }
    }

    fun saveRule(existing: PaymentRule?, input: RuleInput, onDone: () -> Unit, onError: (String) -> Unit) {
        val id = attachedAccountId ?: return
        _uiState.update { it.copy(saving = true, formError = null) }
        viewModelScope.launch {
            try {
                if (existing == null) {
                    repository.createRule(id, input)
                    _uiState.update { it.copy(message = "Правило создано") }
                } else {
                    repository.updateRule(id, existing.id, input)
                    _uiState.update { it.copy(message = "Правило сохранено") }
                }
                _uiState.update { it.copy(saving = false) }
                onDone()
                load()
            } catch (exception: ApiException) {
                _uiState.update { it.copy(saving = false) }
                onError(exception.message)
            }
        }
    }

    fun registerPayment(
        name: String,
        amount: Int,
        direction: PaymentDirection,
        onDone: () -> Unit,
        onError: (String) -> Unit,
    ) {
        val id = attachedAccountId ?: return
        _uiState.update { it.copy(saving = true, formError = null) }
        viewModelScope.launch {
            try {
                repository.registerPayment(id, name, amount, direction)
                _uiState.update { it.copy(saving = false, message = "Платёж добавлен") }
                onDone()
                load()
            } catch (exception: ApiException) {
                _uiState.update { it.copy(saving = false) }
                onError(exception.message)
            }
        }
    }

    fun consumeMessage() = _uiState.update { it.copy(message = null) }
}
