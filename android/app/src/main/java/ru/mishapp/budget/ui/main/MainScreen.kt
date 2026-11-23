package ru.mishapp.budget.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ru.mishapp.budget.R
import ru.mishapp.budget.ui.components.ErrorState
import ru.mishapp.budget.ui.components.LoadingState
import ru.mishapp.budget.ui.forecast.ForecastScreen
import ru.mishapp.budget.ui.history.HistoryScreen
import ru.mishapp.budget.ui.media.MediaScreen
import ru.mishapp.budget.ui.navigation.Routes
import ru.mishapp.budget.ui.payments.PaymentsScreen

private data class BottomTab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    BottomTab(Routes.PAYMENTS, "Платежи", Icons.Default.ShoppingCart),
    BottomTab(Routes.FORECAST, "Прогноз", Icons.Default.DateRange),
    BottomTab(Routes.HISTORY, "История", Icons.AutoMirrored.Filled.List),
    BottomTab(Routes.MEDIA, "Кино", Icons.Default.PlayArrow),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    var accountMenu by remember { mutableStateOf(false) }
    var overflowMenu by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        if (state.accounts.size > 1) {
                            Text(
                                text = state.currentAccount?.name.orEmpty(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                actions = {
                    if (state.accounts.size > 1) {
                        IconButton(onClick = { accountMenu = true }) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Выбрать счёт")
                        }
                        DropdownMenu(expanded = accountMenu, onDismissRequest = { accountMenu = false }) {
                            state.accounts.forEach { account ->
                                DropdownMenuItem(
                                    text = { Text(account.name) },
                                    onClick = {
                                        viewModel.selectAccount(account.id)
                                        accountMenu = false
                                    },
                                )
                            }
                        }
                    }
                    IconButton(onClick = { overflowMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Ещё")
                    }
                    DropdownMenu(expanded = overflowMenu, onDismissRequest = { overflowMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Обновить") },
                            leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null) },
                            onClick = {
                                viewModel.loadAccounts()
                                overflowMenu = false
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Выйти") },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null) },
                            onClick = {
                                overflowMenu = false
                                viewModel.logout()
                            },
                        )
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = backStackEntry?.destination
                tabs.forEach { tab ->
                    val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(Routes.PAYMENTS) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                    )
                }
            }
        },
    ) { padding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(padding)

        when {
            state.loading -> LoadingState(contentModifier)
            state.error != null -> Column(
                modifier = contentModifier,
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ErrorState(message = state.error.orEmpty(), onRetry = viewModel::loadAccounts)
            }
            state.accounts.isEmpty() -> ErrorState(
                message = "У пользователя нет доступных счетов",
                modifier = contentModifier,
            )
            else -> {
                val accountId = state.currentAccountId!!
                NavHost(
                    navController = navController,
                    startDestination = Routes.PAYMENTS,
                    modifier = contentModifier,
                ) {
                    composable(Routes.PAYMENTS) {
                        PaymentsScreen(
                            accountId = accountId,
                            accountName = state.currentAccount?.name.orEmpty(),
                        )
                    }
                    composable(Routes.FORECAST) { ForecastScreen(accountId = accountId) }
                    composable(Routes.HISTORY) { HistoryScreen(accountId = accountId) }
                    composable(Routes.MEDIA) { MediaScreen(accountId = accountId) }
                }
            }
        }
    }
}
