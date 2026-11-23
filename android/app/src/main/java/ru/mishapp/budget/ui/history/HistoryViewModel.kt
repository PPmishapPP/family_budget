package ru.mishapp.budget.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDateTime
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.mishapp.budget.data.ApiException
import ru.mishapp.budget.data.BudgetRepository
import ru.mishapp.budget.domain.HistoryEntry
import ru.mishapp.budget.util.formatMonthYear

enum class HistorySort(val apiKey: String, val label: String) {
    DATE("date", "По дате"),
    SUM("sum", "По сумме"),
    BALANCE("balance", "По балансу"),
    COMMENT("comment", "По комментарию"),
}

enum class SortDirection(val apiValue: String, val label: String) {
    DESC("desc", "Сначала новые"),
    ASC("asc", "Сначала старые"),
}

data class HistoryUiState(
    val loading: Boolean = true,
    val loadingMore: Boolean = false,
    val entries: List<HistoryEntry> = emptyList(),
    val page: Int = 0,
    val totalPages: Int = 0,
    val totalElements: Long = 0,
    val sort: HistorySort = HistorySort.DATE,
    val direction: SortDirection = SortDirection.DESC,
    val error: String? = null,
) {
    val hasMore: Boolean get() = page + 1 < totalPages
}

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: BudgetRepository,
) : ViewModel() {

    private var accountId: Long? = null
    private var loading = false

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    fun attach(id: Long) {
        if (accountId == id) return
        accountId = id
        reload()
    }

    fun setSort(sort: HistorySort) {
        _uiState.update {
            if (it.sort == sort) it.copy(direction = it.direction.flipped()) else it.copy(sort = sort)
        }
        reload()
    }

    fun setDirection(direction: SortDirection) {
        _uiState.update { it.copy(direction = direction) }
        reload()
    }

    fun reload() {
        val id = accountId ?: return
        loading = true
        _uiState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                val state = _uiState.value
                val page = repository.history(id, 0, PAGE_SIZE, state.sort.apiKey, state.direction.apiValue)
                _uiState.update {
                    it.copy(
                        loading = false,
                        entries = page.entries,
                        page = page.page,
                        totalPages = page.totalPages,
                        totalElements = page.totalElements,
                    )
                }
            } catch (exception: ApiException) {
                _uiState.update { it.copy(loading = false, error = exception.message) }
            } finally {
                loading = false
            }
        }
    }

    fun loadMore() {
        val id = accountId ?: return
        val state = _uiState.value
        if (loading || !state.hasMore) return
        loading = true
        _uiState.update { it.copy(loadingMore = true) }
        viewModelScope.launch {
            try {
                val next = state.page + 1
                val page = repository.history(id, next, PAGE_SIZE, state.sort.apiKey, state.direction.apiValue)
                _uiState.update {
                    it.copy(
                        loadingMore = false,
                        entries = it.entries + page.entries,
                        page = page.page,
                        totalPages = page.totalPages,
                    )
                }
            } catch (exception: ApiException) {
                _uiState.update { it.copy(loadingMore = false, error = exception.message) }
            } finally {
                loading = false
            }
        }
    }

    private companion object {
        const val PAGE_SIZE = 30
    }
}

/** Элемент плоского списка истории: либо заголовок месяца, либо операция. */
sealed interface HistoryRow {
    data class Header(val title: String) : HistoryRow
    data class Entry(val entry: HistoryEntry) : HistoryRow
}

/** Разворачивает операции в плоский список с заголовками месяцев. */
fun List<HistoryEntry>.withMonthHeaders(): List<HistoryRow> {
    val rows = mutableListOf<HistoryRow>()
    var lastMonth: String? = null
    forEach { entry ->
        val month = formatMonthYear(entry.dateTime.toLocalDate())
        if (month != lastMonth) {
            lastMonth = month
            rows += HistoryRow.Header(month)
        }
        rows += HistoryRow.Entry(entry)
    }
    return rows
}

private fun SortDirection.flipped(): SortDirection =
    if (this == SortDirection.DESC) SortDirection.ASC else SortDirection.DESC
