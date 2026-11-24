package ru.mishapp.budget.ui.media

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
import ru.mishapp.budget.domain.MediaInput
import ru.mishapp.budget.domain.MediaItem
import ru.mishapp.budget.domain.MediaStatus
import ru.mishapp.budget.domain.Reference

data class MediaUiState(
    val loading: Boolean = true,
    val items: List<MediaItem> = emptyList(),
    val types: List<Reference> = emptyList(),
    val statuses: List<MediaStatus> = emptyList(),
    val typeFilter: String? = null,
    val statusFilter: String? = null,
    val error: String? = null,
    val message: String? = null,
    /** Идёт сохранение формы: кнопка блокируется от двойного нажатия (H-05). */
    val saving: Boolean = false,
) {
    val visibleItems: List<MediaItem>
        get() = items
            .filter { typeFilter == null || it.type == typeFilter }
            .filter { statusFilter == null || it.status == statusFilter }

    fun typeLabel(value: String?): String =
        types.firstOrNull { it.value == value }?.label ?: fallbackTypeLabel(value)

    fun statusLabel(value: String?): String =
        statuses.firstOrNull { it.value == value }?.label ?: fallbackStatusLabel(value)

    fun transitions(status: String?): List<String> =
        statuses.firstOrNull { it.value == status }?.availableTransitions
            ?.filter { it != status }
            ?: fallbackTransitions(status)
}

@HiltViewModel
class MediaViewModel @Inject constructor(
    private val repository: BudgetRepository,
) : ViewModel() {

    private var accountId: Long? = null

    private val _uiState = MutableStateFlow(MediaUiState())
    val uiState: StateFlow<MediaUiState> = _uiState.asStateFlow()

    fun attach(id: Long) {
        if (accountId == id) return
        accountId = id
        load()
    }

    fun load() {
        val id = accountId ?: return
        _uiState.update { it.copy(loading = it.items.isEmpty(), error = null) }
        viewModelScope.launch {
            try {
                val types = if (_uiState.value.types.isEmpty()) repository.mediaTypes() else _uiState.value.types
                val statuses =
                    if (_uiState.value.statuses.isEmpty()) repository.mediaStatuses() else _uiState.value.statuses
                val items = repository.media(id, type = null)
                _uiState.update {
                    it.copy(loading = false, items = items, types = types, statuses = statuses)
                }
            } catch (exception: ApiException) {
                _uiState.update { it.copy(loading = false, error = exception.message) }
            }
        }
    }

    fun setTypeFilter(type: String?) = _uiState.update { it.copy(typeFilter = type) }

    fun setStatusFilter(status: String?) = _uiState.update { it.copy(statusFilter = status) }

    fun save(existing: MediaItem?, input: MediaInput, onDone: () -> Unit, onError: (String) -> Unit) {
        val id = accountId ?: return
        _uiState.update { it.copy(saving = true) }
        viewModelScope.launch {
            try {
                if (existing == null) {
                    repository.createMedia(id, input)
                    _uiState.update { it.copy(message = "Добавлено") }
                } else {
                    repository.updateMedia(id, existing.id, input)
                    _uiState.update { it.copy(message = "Сохранено") }
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

    fun quickStatus(item: MediaItem, status: String) {
        val id = accountId ?: return
        viewModelScope.launch {
            try {
                repository.updateMedia(
                    id,
                    item.id,
                    MediaInput(item.name, item.type, status, item.rating, item.description),
                )
                load()
            } catch (exception: ApiException) {
                _uiState.update { it.copy(message = exception.message) }
            }
        }
    }

    fun delete(item: MediaItem) {
        val id = accountId ?: return
        viewModelScope.launch {
            try {
                repository.deleteMedia(id, item.id)
                _uiState.update { it.copy(message = "Удалено") }
                load()
            } catch (exception: ApiException) {
                _uiState.update { it.copy(message = exception.message) }
            }
        }
    }

    fun consumeMessage() = _uiState.update { it.copy(message = null) }
}

internal const val FALLBACK_TYPE_MOVIE = "MOVIE"
internal const val FALLBACK_TYPE_SERIES = "SERIES"

private fun fallbackTypeLabel(value: String?): String = when (value) {
    FALLBACK_TYPE_SERIES -> "Сериал"
    else -> "Фильм"
}

internal fun fallbackStatusLabel(value: String?): String = when (value) {
    "PLAN_TO_WATCH" -> "Ожидает просмотра"
    "IN_PROGRESS" -> "В процессе"
    "WATCHED" -> "Просмотрено"
    "WAITING_FOR_CONTINUATION" -> "Ожидает продолжения"
    "STOPPED" -> "Остановлен просмотр"
    "CANCELED" -> "Отмена просмотра"
    else -> value.orEmpty()
}

internal fun fallbackTransitions(status: String?): List<String> = when (status) {
    "PLAN_TO_WATCH" -> listOf("IN_PROGRESS", "WATCHED", "WAITING_FOR_CONTINUATION", "CANCELED")
    "IN_PROGRESS" -> listOf("PLAN_TO_WATCH", "WATCHED", "WAITING_FOR_CONTINUATION", "STOPPED", "CANCELED")
    "WATCHED" -> listOf("IN_PROGRESS", "WAITING_FOR_CONTINUATION")
    "WAITING_FOR_CONTINUATION" -> listOf("IN_PROGRESS", "WATCHED", "STOPPED", "CANCELED")
    "STOPPED" -> listOf("IN_PROGRESS", "PLAN_TO_WATCH", "WATCHED", "WAITING_FOR_CONTINUATION", "CANCELED")
    "CANCELED" -> listOf("IN_PROGRESS", "PLAN_TO_WATCH")
    else -> emptyList()
}
