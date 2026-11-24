package ru.mishapp.budget.ui.payments

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import ru.mishapp.budget.domain.PaymentRule
import ru.mishapp.budget.ui.components.ConfirmDeleteDialog
import ru.mishapp.budget.ui.components.EmptyState
import ru.mishapp.budget.ui.components.ErrorState
import ru.mishapp.budget.util.formatBalance
import ru.mishapp.budget.util.formatDate
import ru.mishapp.budget.util.formatSignedBalance
import ru.mishapp.budget.util.pluralize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentsScreen(
    accountId: Long,
    accountName: String,
    viewModel: PaymentsViewModel = hiltViewModel(),
) {
    LaunchedEffect(accountId) { viewModel.attach(accountId) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var editorRule by remember { mutableStateOf<PaymentRule?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var showInstant by remember { mutableStateOf(false) }
    var showActions by remember { mutableStateOf(false) }
    var ruleMenuFor by remember { mutableStateOf<Long?>(null) }
    var ruleToDelete by remember { mutableStateOf<PaymentRule?>(null) }
    var editorError by remember { mutableStateOf<String?>(null) }
    var instantError by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }

    LaunchedEffect(state.message) {
        val message = state.message ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = message,
            actionLabel = if (message.contains("удалено")) "Вернуть" else null,
            withDismissAction = true,
        )
        if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
            viewModel.undoDelete()
        }
        viewModel.consumeMessage()
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        when {
            state.loading -> Box(Modifier.fillMaxWidth().padding(top = 80.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.error != null && state.rules.isEmpty() -> ErrorState(
                message = state.error.orEmpty(),
                onRetry = viewModel::load,
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item { BalanceCard(balance = state.balance, dueCount = state.dueCount) }
                item { SortRow(state = state, onSort = viewModel::setSort, onToggleDue = viewModel::toggleOnlyDue) }

                if (state.visibleRules.isEmpty()) {
                    item {
                        EmptyState(
                            title = if (state.onlyDue) "Нет платежей к оплате" else "Правил пока нет",
                            subtitle = "Добавьте правило кнопкой ниже",
                        )
                    }
                }

                items(state.visibleRules, key = { it.id }) { rule ->
                    RuleCard(
                        rule = rule,
                        busy = state.busyRuleId == rule.id,
                        menuExpanded = ruleMenuFor == rule.id,
                        onMenuOpen = { ruleMenuFor = rule.id },
                        onMenuDismiss = { ruleMenuFor = null },
                        onExecute = {
                            ruleMenuFor = null
                            viewModel.execute(rule)
                        },
                        onEdit = {
                            ruleMenuFor = null
                            editorRule = rule
                            showEditor = true
                        },
                        onDelete = {
                            ruleMenuFor = null
                            ruleToDelete = rule
                        },
                    )
                }
            }
        }

        androidx.compose.material3.SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )

        ExtendedFloatingActionButton(
            onClick = { showActions = true },
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            text = { Text("Добавить") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
        )
    }

    if (showActions) {
        AddActionsSheet(
            onDismiss = { showActions = false },
            onNewRule = {
                showActions = false
                editorRule = null
                showEditor = true
            },
            onInstantPayment = {
                showActions = false
                showInstant = true
            },
        )
    }

    if (showEditor) {
        RuleEditorSheet(
            rule = editorRule,
            submitting = state.saving,
            serverError = editorError,
            onDismiss = { showEditor = false; editorError = null },
            onSave = { input ->
                editorError = null
                viewModel.saveRule(editorRule, input, onDone = { showEditor = false }, onError = { editorError = it })
            },
        )
    }

    if (showInstant) {
        InstantPaymentSheet(
            accountName = accountName,
            submitting = state.saving,
            serverError = instantError,
            onDismiss = { showInstant = false; instantError = null },
            onSave = { name, amount, direction ->
                instantError = null
                viewModel.registerPayment(
                    name = name,
                    amount = amount,
                    direction = direction,
                    onDone = { showInstant = false },
                    onError = { instantError = it },
                )
            },
        )
    }

    ruleToDelete?.let { rule ->
        ConfirmDeleteDialog(
            title = "Удалить правило?",
            text = "Правило «${rule.name}» будет удалено.",
            onConfirm = {
                ruleToDelete = null
                viewModel.delete(rule)
            },
            onDismiss = { ruleToDelete = null },
        )
    }
}

@Composable
private fun BalanceCard(balance: Int, dueCount: Int) {
    val scheme = MaterialTheme.colorScheme
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.primaryContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
        ) {
            Column {
                Text(
                    text = "Текущий баланс",
                    style = MaterialTheme.typography.labelLarge,
                    color = scheme.onPrimaryContainer.copy(alpha = 0.75f),
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = formatBalance(balance),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onPrimaryContainer,
                )
                Spacer(Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = scheme.surface.copy(alpha = 0.35f),
                    contentColor = scheme.onPrimaryContainer,
                ) {
                    Text(
                        text = if (dueCount == 0) {
                            "Нет платежей к оплате"
                        } else {
                            "$dueCount ${pluralize(dueCount, "платёж", "платежа", "платежей")} к оплате"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortRow(state: PaymentsUiState, onSort: (RuleSort) -> Unit, onToggleDue: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilterChip(
            selected = state.onlyDue,
            onClick = onToggleDue,
            label = { Text("К оплате") },
        )
        RuleSort.entries.forEach { sort ->
            val selected = state.sort == sort
            FilterChip(
                selected = selected,
                onClick = { onSort(sort) },
                label = { Text(sort.label) },
                trailingIcon = if (selected) {
                    {
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = if (state.ascending) "по возрастанию" else "по убыванию",
                            modifier = Modifier.size(18.dp),
                        )
                    }
                } else {
                    null
                },
            )
        }
    }
}

@Composable
private fun RuleCard(
    rule: PaymentRule,
    busy: Boolean,
    menuExpanded: Boolean,
    onMenuOpen: () -> Unit,
    onMenuDismiss: () -> Unit,
    onExecute: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val due = rule.isDue
    val onContainer = if (due) scheme.onErrorContainer else scheme.onSurfaceVariant

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (due) scheme.errorContainer else scheme.surfaceVariant,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 6.dp, top = 14.dp, bottom = 4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = rule.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = onContainer,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = formatSignedBalance(rule.sum),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = when {
                        due -> onContainer
                        rule.sum >= 0 -> scheme.primary
                        else -> scheme.error
                    },
                )
            }

            Spacer(Modifier.height(4.dp))
            Text(
                text = buildString {
                    append(rule.type.label)
                    rule.endDate?.let { append(" · до ${formatDate(it)}") }
                },
                style = MaterialTheme.typography.bodySmall,
                color = onContainer.copy(alpha = 0.8f),
            )

            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                DueBadge(rule = rule, due = due)
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onExecute, enabled = !busy) {
                    if (busy) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Провести")
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
        }
    }
}

/** Наглядно показывает, когда ждать платёж: «Сегодня», «Просрочено» или «12.01.2027». */
@Composable
private fun DueBadge(rule: PaymentRule, due: Boolean) {
    val scheme = MaterialTheme.colorScheme
    val today = java.time.LocalDate.now()
    val (text, container, content) = when {
        rule.nextDay == today -> Triple("Сегодня", scheme.error, scheme.onError)
        due -> Triple("Просрочено", scheme.error, scheme.onError)
        else -> Triple("Следующий: ${formatDate(rule.nextDay)}", scheme.surface, scheme.onSurfaceVariant)
    }

    Surface(
        shape = RoundedCornerShape(50),
        color = container,
        contentColor = content,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        ) {
            if (due) {
                Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
            }
            Text(text = text, style = MaterialTheme.typography.labelMedium)
        }
    }
}
