package ru.mishapp.budget.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import ru.mishapp.budget.data.AuthState
import ru.mishapp.budget.data.SessionManager
import ru.mishapp.budget.ui.login.LoginScreen
import ru.mishapp.budget.ui.main.MainScreen

@Composable
fun FamilyBudgetApp(sessionManager: SessionManager) {
    val state by sessionManager.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        if (state is AuthState.Loading) {
            sessionManager.bootstrap()
        }
    }

    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Crossfade(targetState = state, label = "auth") { current ->
            when (current) {
                AuthState.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
                AuthState.LoggedOut -> LoginScreen()
                is AuthState.LoggedIn -> MainScreen()
            }
        }
    }
}
