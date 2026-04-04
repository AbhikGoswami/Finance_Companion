package com.abhik.financecompanion

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.abhik.financecompanion.ui.LoginScreen
import com.abhik.financecompanion.ui.MainScreen
import com.abhik.financecompanion.ui.theme.FinanceCompanionTheme
import com.abhik.financecompanion.viewmodel.FinanceViewModel
import com.abhik.financecompanion.viewmodel.FinanceViewModelFactory
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

class MainActivity : FragmentActivity() {

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
                val bioEnabled by viewModel.isBiometricEnabled.collectAsState()

                var isUnlocked by remember { mutableStateOf(false) }

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
                    }
                    else{

                        if (bioEnabled && !isUnlocked) {
                            LaunchedEffect(Unit) {
                                showBiometricPrompt(this@MainActivity) {
                                    isUnlocked = true
                                }
                            }

                        }
                        else{
                            MainScreen(
                                viewModel = viewModel,
                                userEmail = currentUser?.email ?: "Unknown User",
                                onSignOut = {
                                    auth.signOut()
                                    GoogleSignIn.getClient(context, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut()
                                    currentUser = null
                                    isUnlocked = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    private fun showBiometricPrompt(activity: FragmentActivity, onAuthenticated: () -> Unit) {
        val executor = ContextCompat.getMainExecutor(activity)
        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onAuthenticated()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(activity, "Authentication Required", Toast.LENGTH_SHORT).show()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Finance Companion Lock")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Cancel")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}