package ru.mishapp.budget.ui.media

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ru.mishapp.budget.domain.MediaInput
import ru.mishapp.budget.domain.MediaItem
import ru.mishapp.budget.domain.MediaStatus
import ru.mishapp.budget.domain.Reference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaEditorSheet(
    item: MediaItem?,
    types: List<Reference>,
    statuses: List<MediaStatus>,
    submitting: Boolean,
    serverError: String?,
    onDismiss: () -> Unit,
    onSave: (MediaInput) -> Unit,
) {
    val typeOptions = types.ifEmpty {
        listOf(Reference("MOVIE", "Фильм"), Reference("SERIES", "Сериал"))
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name by remember { mutableStateOf(item?.name.orEmpty()) }
    var type by remember { mutableStateOf(item?.type ?: typeOptions.first().value) }
    var status by remember { mutableStateOf(item?.status ?: "PLAN_TO_WATCH") }
    var ratingText by remember { mutableStateOf(item?.rating?.toString().orEmpty()) }
    var description by remember { mutableStateOf(item?.description.orEmpty()) }
    var typeMenu by remember { mutableStateOf(false) }
    var statusMenu by remember { mutableStateOf(false) }

    val statusLabel = statuses.firstOrNull { it.value == status }?.label
        ?: fallbackStatusLabel(status)
    val rating = ratingText.filter { it.isDigit() }.toIntOrNull()?.takeIf { it in 1..10 }

    // Как в web: для новой записи доступны все статусы, при редактировании — только
    // допустимые переходы из текущего статуса (G-05, G-06).
    val statusOptions = if (item == null) {
        statuses.ifEmpty {
            listOf("PLAN_TO_WATCH", "IN_PROGRESS", "WATCHED", "WAITING_FOR_CONTINUATION", "STOPPED", "CANCELED")
                .map { MediaStatus(it, fallbackStatusLabel(it), emptyList()) }
        }
    } else {
        val allowed = statuses.firstOrNull { it.value == item.status }?.availableTransitions
            ?.filter { it != item.status }
            ?: fallbackTransitions(item.status)
        allowed.map { value ->
            MediaStatus(value, statuses.firstOrNull { it.value == value }?.label ?: fallbackStatusLabel(value), emptyList())
        }
    }

    ModalBottomSheet(
        onDismissRequest = { if (!submitting) onDismiss() },
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
        ) {
            Text(
                text = if (item == null) "Новое" else "Изменить",
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Название") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))

            ExposedDropdownMenuBox(
                expanded = typeMenu,
                onExpandedChange = { typeMenu = !typeMenu },
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = typeOptions.firstOrNull { it.value == type }?.label ?: type,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Тип") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeMenu) },
                    modifier = Modifier
                        .menuAnchor(androidx.compose.material3.ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                )
                ExposedDropdownMenu(
                    expanded = typeMenu,
                    onDismissRequest = { typeMenu = false },
                ) {
                    typeOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.label) },
                            onClick = {
                                type = option.value
                                typeMenu = false
                            },
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            ExposedDropdownMenuBox(
                expanded = statusMenu,
                onExpandedChange = { statusMenu = !statusMenu },
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = statusLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Статус") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusMenu) },
                    modifier = Modifier
                        .menuAnchor(androidx.compose.material3.ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                )
                ExposedDropdownMenu(
                    expanded = statusMenu,
                    onDismissRequest = { statusMenu = false },
                ) {
                    statusOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.label) },
                            onClick = {
                                status = option.value
                                statusMenu = false
                            },
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = ratingText,
                    onValueChange = { ratingText = it.filter { ch -> ch.isDigit() }.take(2) },
                    label = { Text("Оценка 1–10") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(140.dp),
                )
            }
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Описание") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth(),
            )

            if (serverError != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = serverError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    onSave(
                        MediaInput(
                            name = name.trim(),
                            type = type,
                            status = status,
                            rating = rating,
                            description = description.trim().takeIf { it.isNotBlank() },
                        ),
                    )
                },
                enabled = name.isNotBlank() && !submitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                if (submitting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(if (item == null) "Добавить" else "Сохранить")
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusPickerSheet(
    title: String,
    current: String,
    transitions: List<Pair<String, String>>,
    onDismiss: () -> Unit,
    onPick: (String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.navigationBarsPadding()) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 4.dp),
            )
            Text(
                text = "Текущий статус: $current",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 12.dp),
            )
            transitions.forEach { (value, label) ->
                ListItem(
                    headlineContent = { Text(label) },
                    leadingContent = { Icon(Icons.Default.Check, contentDescription = null) },
                    colors = androidx.compose.material3.ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    modifier = Modifier.clickable { onPick(value) },
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
