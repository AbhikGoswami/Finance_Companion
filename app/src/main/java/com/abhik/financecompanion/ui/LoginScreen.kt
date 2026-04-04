package com.abhik.financecompanion.ui

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image // Added
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource // Added
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhik.financecompanion.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun LoginScreen(onLoginSuccess: (FirebaseUser) -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ){ result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try{
                val account = task.getResult(ApiException::class.java)!!
                val idToken = account.idToken

                if (idToken != null) {
                    val credential = GoogleAuthProvider.getCredential(idToken, null)
                    coroutineScope.launch {
                        try {
                            val authResult = FirebaseAuth.getInstance().signInWithCredential(credential).await()
                            authResult.user?.let { onLoginSuccess(it) }
                        }
                        catch (e: Exception) {
                            errorMessage = e.localizedMessage
                            isLoading = false
                        }
                    }
                }
                else{
                    errorMessage = "Google Sign-In failed: No ID Token found."
                    isLoading = false
                }
            }
            catch (e: ApiException) {
                errorMessage = "Google Sign-In failed. Did you add your SHA-1 key to Firebase?"
                isLoading = false
            }
        }
        else{
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Image(
                painter = painterResource(id = R.drawable.finance_app_icon),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 16.dp)
            )
            Text(
                text = "Finance Companion",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Manage your money effortlessly.", color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(48.dp))

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        isLoading = true
                        errorMessage = null

                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(context.getString(R.string.default_web_client_id))
                            .requestEmail()
                            .build()
                        val googleSignInClient = GoogleSignIn.getClient(context, gso)

                        googleSignInClient.signOut().addOnCompleteListener {
                            launcher.launch(googleSignInClient.signInIntent)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.8f).height(50.dp)
                ) {
                    Text("Sign in with Google", fontSize = 16.sp)
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}