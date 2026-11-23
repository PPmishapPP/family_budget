package ru.mishapp.budget.ui.history

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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.distinctUntilChanged
import ru.mishapp.budget.domain.HistoryEntry
import ru.mishapp.budget.ui.components.EmptyState
import ru.mishapp.budget.ui.components.ErrorState
import ru.mishapp.budget.util.formatDateTime
import ru.mishapp.budget.util.formatSignedBalance
import ru.mishapp.budget.util.pluralize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    accountId: Long,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    LaunchedEffect(accountId) { viewModel.attach(accountId) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val rows = remember(state.entries) { state.entries.withMonthHeaders() }

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = listState.layoutInfo.totalItemsCount
            total > 0 && lastVisible >= total - 3
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { shouldLoadMore }
            .distinctUntilChanged()
            .collect { if (it) viewModel.loadMore() }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HistorySort.entries.forEach { sort ->
                    FilterChip(
                        selected = state.sort == sort,
                        onClick = { viewModel.setSort(sort) },
                        label = { Text(sort.label) },
                    )
                }
                FilterChip(
                    selected = true,
                    onClick = { viewModel.setDirection(state.direction.flip()) },
                    label = { Text(if (state.direction == SortDirection.DESC) "↓" else "↑") },
                    leadingIcon = {
                        Icon(
                            if (state.direction == SortDirection.DESC) Icons.Default.ArrowDownward
                            else Icons.Default.ArrowUpward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                    },
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = "${state.totalElements} " +
                    pluralize(state.totalElements.toInt(), "операция", "операции", "операций"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        when {
            state.loading -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            state.error != null && state.entries.isEmpty() ->
                ErrorState(message = state.error.orEmpty(), onRetry = viewModel::reload)

            state.entries.isEmpty() -> EmptyState(title = "История пуста")

            else -> LazyColumn(
                state = listState,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(
                    items = rows,
                    key = { row ->
                        when (row) {
                            is HistoryRow.Header -> "h-${row.title}"
                            is HistoryRow.Entry -> row.entry.id
                        }
                    },
                ) { row ->
                    when (row) {
                        is HistoryRow.Header -> Text(
                            text = row.title,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp, bottom = 2.dp),
                        )
                        is HistoryRow.Entry -> HistoryCard(row.entry)
                    }
                }
                if (state.loadingMore) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center,
                        ) { CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp) }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryCard(entry: HistoryEntry) {
    val income = entry.sum >= 0
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
                Text(
                    text = entry.comment?.takeIf { it.isNotBlank() } ?: "—",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = formatDateTime(entry.dateTime),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (income) "+${formatSignedBalance(entry.sum)}" else formatSignedBalance(entry.sum),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (income) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                )
                Text(
                    text = "Баланс: ${formatSignedBalance(entry.balance)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun SortDirection.flip(): SortDirection =
    if (this == SortDirection.DESC) SortDirection.ASC else SortDirection.DESC
