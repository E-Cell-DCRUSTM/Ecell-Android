package dcrustm.ecell.mobile.ui.onboarding.signin

import android.annotation.SuppressLint
import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.hilt.navigation.compose.hiltViewModel
import dcrustm.ecell.mobile.ui.onboarding.AuthState
import dcrustm.ecell.mobile.ui.onboarding.AuthViewModel

@SuppressLint("ContextCastToActivity")
@Composable
fun SignInApp(
    credentialManager: CredentialManager,
    onSuccessfulLoginEvent: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: AuthViewModel = hiltViewModel()
    val context = LocalContext.current as Activity

    val authState by viewModel.authState.collectAsState()

    // Observe changes in authState to trigger toast messages and navigation callback.
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                Toast.makeText(context, "Authentication Successful", Toast.LENGTH_SHORT).show()
                onSuccessfulLoginEvent()
            }
            is AuthState.Error -> {
                Toast.makeText(context, (authState as AuthState.Error).message, Toast.LENGTH_SHORT).show()
            }
            else -> Unit
        }
    }


    Box(modifier = modifier.fillMaxSize()) {
        // Your SignInScreen remains the same, just trigger the viewModel functions.
        SignInScreen(
            onGoogleSignupClick = { viewModel.signUpWithGoogle(credentialManager, context) },
            onGoogleLoginClick = { viewModel.loginWithGoogle(credentialManager, context) },
            onEmailSignupClick = { userData ->
                viewModel.signUpWithEmail(userData)
            },
            onEmailLoginClick = { email, password ->
                viewModel.loginWithEmail(email, password)
            }
        )

        // Show a full-screen loading indicator when in Loading state.
        if (authState is AuthState.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color.Black,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}