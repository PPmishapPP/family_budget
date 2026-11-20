package ru.mishapp.budget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import ru.mishapp.budget.ui.FamilyBudgetApp
import ru.mishapp.budget.ui.theme.FamilyBudgetTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FamilyBudgetTheme {
                FamilyBudgetApp()
            }
        }
    }
}
