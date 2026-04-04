package com.abhik.financecompanion

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.abhik.financecompanion.ui.LoginScreen
import com.abhik.financecompanion.ui.MainScreen
import com.abhik.financecompanion.ui.theme.FinanceCompanionTheme
import com.abhik.financecompanion.viewmodel.FinanceViewModel
import com.abhik.financecompanion.viewmodel.FinanceViewModelFactory
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinanceCompanionTheme {
                val app = application as FinanceApplication
                val sharedPreferences = getSharedPreferences("finance_prefs", Context.MODE_PRIVATE)

                val viewModel: FinanceViewModel = viewModel(
                    factory = FinanceViewModelFactory(app.repository, sharedPreferences)
                )

                val context = LocalContext.current
                val auth = FirebaseAuth.getInstance()

                var currentUser by remember { mutableStateOf(auth.currentUser) }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (currentUser == null) {

                        LoginScreen(
                            onLoginSuccess = { user ->
                                currentUser = user
                            }
                        )
                    } else {

                        MainScreen(
                            viewModel = viewModel,
                            userEmail = currentUser?.email ?: "Unknown User",
                            onSignOut = {

                                auth.signOut()
                                GoogleSignIn.getClient(context, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut()
                                currentUser = null
                            }
                        )
                    }
                }
            }
        }

    }
}