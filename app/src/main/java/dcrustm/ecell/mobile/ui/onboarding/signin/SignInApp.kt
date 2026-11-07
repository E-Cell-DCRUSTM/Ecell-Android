package dcrustm.ecell.mobile.ui.onboarding.signin

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.hilt.navigation.compose.hiltViewModel
import dcrustm.ecell.mobile.ui.onboarding.AuthState
import dcrustm.ecell.mobile.ui.onboarding.AuthViewModel

tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun SignInApp(
    credentialManager: CredentialManager,
    onSuccessfulLoginEvent: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: AuthViewModel = hiltViewModel()
    val context = LocalContext.current
    val activity = context.findActivity() ?: return

    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                onSuccessfulLoginEvent()
            }
            else -> Unit
        }
    }


    Box(modifier = modifier.fillMaxSize()) {
        SignInScreen(
            onContinueWithGoogleClick = { viewModel.continueWithGoogle(credentialManager, activity) },
            onEmailSignInClick = { email, pass -> viewModel.signInWithEmail(email, pass) },
            onTestSignInClick = { viewModel.signInAsTestUser() },
            isLoading = authState is AuthState.Loading,
            errorMessage = (authState as? AuthState.Error)?.message
        )
    }
}
