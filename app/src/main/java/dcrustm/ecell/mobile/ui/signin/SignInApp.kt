package dcrustm.ecell.mobile.ui.signin

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import dcrustm.ecell.mobile.ui.AuthViewModel

@Composable
fun SignInApp(modifier: Modifier = Modifier) {
    val viewModel: AuthViewModel = hiltViewModel()

    // TODO: Implement a bottom bar for email address. For next iteration, let's authenticate the email with an otp.

    SignInScreen(
        onGoogleButtonClick = {},
        onAccountAlreadyClick = {},
        onEmailButtonClick = {}
    )

}