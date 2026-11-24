package ru.mishapp.budget.ui.payments

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import ru.mishapp.budget.domain.PaymentDirection
import ru.mishapp.budget.domain.PaymentRule
import ru.mishapp.budget.domain.RuleInput
import ru.mishapp.budget.domain.RuleType
import ru.mishapp.budget.util.formatDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddActionsSheet(
    onDismiss: () -> Unit,
    onNewRule: () -> Unit,
    onInstantPayment: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.navigationBarsPadding()) {
            Text(
                text = "Что добавить?",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 8.dp),
            )
            ListItem(
                headlineContent = { Text("Новое правило") },
                supportingContent = { Text("Периодический платёж: ежемесячно, еженедельно…") },
                leadingContent = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.clickableRow(onNewRule),
            )
            ListItem(
                headlineContent = { Text("Разовый платёж") },
                supportingContent = { Text("Доход или расход прямо сейчас") },
                leadingContent = { Icon(Icons.Default.Payments, contentDescription = null) },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.clickableRow(onInstantPayment),
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

private fun Modifier.clickableRow(onClick: () -> Unit): Modifier =
    this.clickable(onClick = onClick)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuleEditorSheet(
    rule: PaymentRule?,
    submitting: Boolean,
    serverError: String?,
    onDismiss: () -> Unit,
    onSave: (RuleInput) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by remember { mutableStateOf(rule?.name.orEmpty()) }
    var sumText by remember { mutableStateOf(rule?.sum?.let { kotlin.math.abs(it) }?.toString().orEmpty()) }
    var direction by remember {
        mutableStateOf(
            if ((rule?.sum ?: 0) > 0) PaymentDirection.INCOME else PaymentDirection.EXPENSE,
        )
    }
    var type by remember { mutableStateOf(rule?.type ?: RuleType.MONTHLY) }
    var nextDay by remember { mutableStateOf(rule?.nextDay ?: LocalDate.now()) }
    var endDate by remember { mutableStateOf(rule?.endDate) }
    var showNextPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val magnitude = sumText.filter { it.isDigit() }.toIntOrNull()
    val canSave = name.isNotBlank() && magnitude != null && magnitude > 0 && !submitting
    val signedSum = magnitude?.let { signedRuleSum(direction, it) }
    val shownError = error ?: serverError

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
                text = if (rule == null) "Новое правило" else "Изменить правило",
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(256); error = null },
                label = { Text("Название") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                PaymentDirection.entries.forEachIndexed { index, option ->
                    SegmentedButton(
                        selected = direction == option,
                        onClick = { direction = option },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = PaymentDirection.entries.size,
                        ),
                    ) {
                        Text(option.label)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = sumText,
                onValueChange = { sumText = it.filter { ch -> ch.isDigit() }.take(9); error = null },
                label = { Text(if (direction == PaymentDirection.EXPENSE) "Сумма расхода, ₽" else "Сумма дохода, ₽") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))

            Text("Периодичность", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                RuleType.entries.forEach { option ->
                    FilterChip(
                        selected = type == option,
                        onClick = { type = option },
                        label = { Text(option.label) },
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = formatDate(nextDay),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Следующий платёж") },
                    modifier = Modifier
                        .weight(1f)
                        .clickableRow { showNextPicker = true },
                )
                Spacer(Modifier.width(12.dp))
                OutlinedTextField(
                    value = endDate?.let(::formatDate) ?: "Без ограничения",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Окончание") },
                    modifier = Modifier
                        .weight(1f)
                        .clickableRow { showEndPicker = true },
                )
            }
            if (endDate != null) {
                TextButton(onClick = { endDate = null }) { Text("Убрать дату окончания") }
            }

            if (shownError != null) {
                Text(
                    text = shownError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    val amount = signedSum
                    if (amount == null || amount == 0) {
                        error = "Укажите сумму больше нуля"
                        return@Button
                    }
                    onSave(
                        RuleInput(
                            name = name.trim(),
                            sum = amount,
                            type = type,
                            nextDay = nextDay,
                            endDate = endDate,
                        ),
                    )
                },
                enabled = canSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                if (submitting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(if (rule == null) "Создать" else "Сохранить")
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    if (showNextPicker) {
        DatePickerSheet(
            initial = nextDay,
            onDismiss = { showNextPicker = false },
            onPick = { nextDay = it; showNextPicker = false },
        )
    }
    if (showEndPicker) {
        DatePickerSheet(
            initial = endDate ?: LocalDate.now(),
            onDismiss = { showEndPicker = false },
            onPick = { endDate = it; showEndPicker = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerSheet(
    initial: LocalDate,
    onDismiss: () -> Unit,
    onPick: (LocalDate) -> Unit,
) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initial.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli(),
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val millis = state.selectedDateMillis
                    if (millis != null) {
                        onPick(Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate())
                    } else {
                        onDismiss()
                    }
                },
            ) { Text("ОК") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
    ) {
        DatePicker(state = state)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstantPaymentSheet(
    accountName: String,
    submitting: Boolean,
    serverError: String?,
    onDismiss: () -> Unit,
    onSave: (name: String, amount: Int, direction: PaymentDirection) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by remember { mutableStateOf("") }
    var sumText by remember { mutableStateOf("") }
    var direction by remember { mutableStateOf(PaymentDirection.EXPENSE) }
    var error by remember { mutableStateOf<String?>(null) }

    val sum = sumText.filter { it.isDigit() }.toIntOrNull()
    val canSave = name.isNotBlank() && sum != null && sum > 0 && !submitting
    val shownError = error ?: serverError

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
            Text("Разовый платёж", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Счёт: $accountName\nДата: ${formatDate(LocalDate.now())}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                PaymentDirection.entries.forEachIndexed { index, option ->
                    SegmentedButton(
                        selected = direction == option,
                        onClick = { direction = option },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = PaymentDirection.entries.size,
                        ),
                    ) {
                        Text(option.label)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(256); error = null },
                label = { Text("Название платежа") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = sumText,
                onValueChange = { sumText = it.filter { ch -> ch.isDigit() }; error = null },
                label = { Text("Сумма, ₽") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )

            if (shownError != null) {
                Text(
                    text = shownError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    val amount = sum
                    if (amount == null || amount <= 0) {
                        error = "Укажите сумму больше нуля"
                        return@Button
                    }
                    onSave(name.trim(), amount, direction)
                },
                enabled = canSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                if (submitting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Добавить")
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
