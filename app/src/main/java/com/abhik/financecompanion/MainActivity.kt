package com.abhik.financecompanion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.abhik.financecompanion.ui.MainScreen
import com.abhik.financecompanion.ui.theme.FinanceCompanionTheme
import com.abhik.financecompanion.viewmodel.FinanceViewModel
import com.abhik.financecompanion.viewmodel.FinanceViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinanceCompanionTheme {

                val app = application as FinanceApplication
                val viewModel: FinanceViewModel = viewModel(
                    factory = FinanceViewModelFactory(app.repository)
                )

                MainScreen(viewModel = viewModel)
            }
        }
    }
}