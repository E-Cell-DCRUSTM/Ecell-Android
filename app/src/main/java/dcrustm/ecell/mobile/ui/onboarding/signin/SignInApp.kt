package dcrustm.ecell.mobile.ui.onboarding.signin

import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.hilt.navigation.compose.hiltViewModel
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

    val isLoginSuccessful = viewModel.isLoginSuccessful.collectAsState()

//    LaunchedEffect(
//        isLoginSuccessful
//    ) {
//
//        if (isLoginSuccessful == True) {
//            onSuccessfulLoginEvent()
//        }
//
//    }

    // TODO: Implement a bottom bar for email address. For next iteration, let's authenticate the email with an otp.

    SignInScreen(
        onGoogleSignupClick = { viewModel.signUpWithGoogle(credentialManager, context) },
        onGoogleLoginClick = { viewModel.loginWithGoogle(credentialManager, context) },

        onEmailSignupClick = { userData ->
            viewModel.signUpWithEmail(userData)
        },
        onEmailLoginClick = {email, password ->
            viewModel.loginWithEmail(email, password)
        }
    )

}