package ru.mishapp.budget.ui.forecast

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import ru.mishapp.budget.ui.components.EmptyState
import ru.mishapp.budget.ui.components.ErrorState
import ru.mishapp.budget.util.formatDate
import ru.mishapp.budget.util.formatMoney
import ru.mishapp.budget.util.formatSignedBalance

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForecastScreen(
    accountId: Long,
    viewModel: ForecastViewModel = hiltViewModel(),
) {
    LaunchedEffect(accountId) { viewModel.attach(accountId) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showPicker by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                ForecastTab.entries.forEachIndexed { index, tab ->
                    SegmentedButton(
                        selected = state.tab == tab,
                        onClick = {
                            viewModel.setTab(tab)
                            viewModel.clear()
                            viewModel.calculate()
                        },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = ForecastTab.entries.size,
                        ),
                    ) { Text(tab.label) }
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = formatDate(state.toDate),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Считать до") },
                    isError = !state.dateValid,
                    supportingText = if (!state.dateValid) {
                        { Text("От завтрашнего дня до +3 лет") }
                    } else {
                        null
                    },
                    leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showPicker = true },
                )
            }

            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(1L to "+1 месяц", 12L to "+1 год", 24L to "+2 года").forEach { (months, label) ->
                    val target = firstOfMonth(months)
                    FilterChip(
                        onClick = { viewModel.setDate(target) },
                        label = { Text(label) },
                        selected = state.toDate == target,
                    )
                }
            }

            if (state.hasRules == false) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "У счёта нет правил платежей — расчёт недоступен",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(12.dp))
            Button(
                onClick = viewModel::calculate,
                enabled = state.canCalculate,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            ) {
                Text(if (state.tab == ForecastTab.PAYMENTS) "Рассчитать прогноз" else "Рассчитать доход")
            }
        }

        when {
            state.loading -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            state.error != null -> ErrorState(message = state.error.orEmpty(), onRetry = viewModel::calculate)

            state.tab == ForecastTab.PAYMENTS -> {
                val items = state.payments
                if (items == null) {
                    EmptyState(title = "Прогноз не рассчитан", subtitle = "Выберите дату и нажмите кнопку")
                } else if (items.isEmpty()) {
                    EmptyState(title = "Прогноз пуст", subtitle = "Платежей по правилам нет")
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(items) { item ->
                            ForecastRow(
                                date = formatDate(item.date),
                                title = item.operation ?: "Операция",
                                sum = item.sum,
                                balance = item.balance,
                            )
                        }
                    }
                }
            }

            else -> {
                val items = state.income
                if (items == null) {
                    EmptyState(title = "Расчёт не выполнен", subtitle = "Выберите дату и нажмите кнопку")
                } else if (items.isEmpty()) {
                    EmptyState(title = "Расчёт пуст", subtitle = "Доходов по правилам нет")
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(items) { item ->
                            ForecastRow(
                                date = formatDate(item.date),
                                title = "Прибавка",
                                sum = item.increase,
                                balance = item.balance,
                            )
                        }
                    }
                }
            }
        }
    }

    if (showPicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.toDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val date = Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneId.of("UTC")).toLocalDate()
                    return !date.isBefore(minForecastDate) && !date.isAfter(maxForecastDate)
                }

                override fun isSelectableYear(year: Int): Boolean =
                    year >= minForecastDate.year && year <= maxForecastDate.year
            },
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let {
                        viewModel.setDate(Instant.ofEpochMilli(it).atZone(ZoneId.of("UTC")).toLocalDate())
                    }
                    showPicker = false
                }) { Text("ОК") }
            },
            dismissButton = { TextButton(onClick = { showPicker = false }) { Text("Отмена") } },
        ) { DatePicker(state = pickerState) }
    }
}

@Composable
private fun ForecastRow(date: String, title: String, sum: Int?, balance: Int) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(date, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = when {
                        sum == null -> "—"
                        sum >= 0 -> formatMoney(sum, signed = true)
                        else -> formatMoney(sum)
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = when {
                        sum == null -> MaterialTheme.colorScheme.onSurfaceVariant
                        sum >= 0 -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.error
                    },
                )
                Text(
                    text = "Баланс: ${formatSignedBalance(balance)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
