package ru.mishapp.budget.ui.media

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.mishapp.budget.domain.MediaItem
import ru.mishapp.budget.ui.components.ConfirmDeleteDialog
import ru.mishapp.budget.ui.components.EmptyState
import ru.mishapp.budget.ui.components.ErrorState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaScreen(
    accountId: Long,
    viewModel: MediaViewModel = hiltViewModel(),
) {
    LaunchedEffect(accountId) { viewModel.attach(accountId) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var editor by remember { mutableStateOf<MediaItem?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var menuFor by remember { mutableStateOf<Long?>(null) }
    var statusPickerFor by remember { mutableStateOf<MediaItem?>(null) }
    var itemToDelete by remember { mutableStateOf<MediaItem?>(null) }
    var editorError by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.message) {
        val message = state.message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message, withDismissAction = true)
        viewModel.consumeMessage()
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = state.typeFilter == null,
                        onClick = { viewModel.setTypeFilter(null) },
                        label = { Text("Все") },
                    )
                    val typeOptions = state.types.ifEmpty {
                        listOf(
                            ru.mishapp.budget.domain.Reference("MOVIE", "Фильм"),
                            ru.mishapp.budget.domain.Reference("SERIES", "Сериал"),
                        )
                    }
                    typeOptions.forEach { type ->
                        FilterChip(
                            selected = state.typeFilter == type.value,
                            onClick = { viewModel.setTypeFilter(type.value) },
                            label = { Text(type.label) },
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    state.statuses.forEach { status ->
                        FilterChip(
                            selected = state.statusFilter == status.value,
                            onClick = {
                                viewModel.setStatusFilter(
                                    if (state.statusFilter == status.value) null else status.value,
                                )
                            },
                            label = { Text(status.label) },
                        )
                    }
                }
            }

            when {
                state.loading -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }

                state.error != null && state.items.isEmpty() ->
                    ErrorState(message = state.error.orEmpty(), onRetry = viewModel::load)

                state.visibleItems.isEmpty() -> EmptyState(
                    title = "Ничего не найдено",
                    subtitle = "Добавьте фильм или сериал кнопкой ниже",
                )

                else -> LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(state.visibleItems, key = { it.id }) { item ->
                        MediaCard(
                            item = item,
                            typeLabel = state.typeLabel(item.type),
                            statusLabel = state.statusLabel(item.status),
                            menuExpanded = menuFor == item.id,
                            onMenuOpen = { menuFor = item.id },
                            onMenuDismiss = { menuFor = null },
                            onEdit = {
                                menuFor = null
                                editor = item
                                showEditor = true
                            },
                            onDelete = {
                                menuFor = null
                                itemToDelete = item
                            },
                            onStatusClick = { statusPickerFor = item },
                        )
                    }
                }
            }
        }

        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))

        ExtendedFloatingActionButton(
            onClick = {
                editor = null
                showEditor = true
            },
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            text = { Text("Добавить") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
        )
    }

    if (showEditor) {
        MediaEditorSheet(
            item = editor,
            types = state.types,
            statuses = state.statuses,
            submitting = state.saving,
            serverError = editorError,
            onDismiss = { showEditor = false; editorError = null },
            onSave = { input ->
                editorError = null
                viewModel.save(editor, input, onDone = { showEditor = false }, onError = { editorError = it })
            },
        )
    }

    statusPickerFor?.let { item ->
        val transitions = state.transitions(item.status)
        LaunchedEffect(transitions) {
            if (transitions.isEmpty()) statusPickerFor = null
        }
        if (transitions.isNotEmpty()) {
            StatusPickerSheet(
                title = item.name,
                current = state.statusLabel(item.status),
                transitions = transitions.map { it to state.statusLabel(it) },
                onDismiss = { statusPickerFor = null },
                onPick = { value ->
                    statusPickerFor = null
                    viewModel.quickStatus(item, value)
                },
            )
        }
    }

    itemToDelete?.let { item ->
        ConfirmDeleteDialog(
            title = "Удалить запись?",
            text = "«${item.name}» будет удалена.",
            onConfirm = {
                itemToDelete = null
                viewModel.delete(item)
            },
            onDismiss = { itemToDelete = null },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MediaCard(
    item: MediaItem,
    typeLabel: String,
    statusLabel: String,
    menuExpanded: Boolean,
    onMenuOpen: () -> Unit,
    onMenuDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onStatusClick: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 14.dp, bottom = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = typeLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (item.rating != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = item.rating.toString(),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                Box {
                    IconButton(onClick = onMenuOpen) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Ещё")
                    }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = onMenuDismiss) {
                        DropdownMenuItem(
                            text = { Text("Изменить") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = onEdit,
                        )
                        DropdownMenuItem(
                            text = { Text("Удалить") },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                            onClick = onDelete,
                        )
                    }
                }
            }

            if (!item.description.isNullOrBlank()) {
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, end = 8.dp),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AssistChip(
                    onClick = onStatusClick,
                    label = { Text(statusLabel) },
                    colors = AssistChipDefaults.assistChipColors(
                        labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    ),
                )
            }
        }
    }
}
