package dcrustm.ecell.mobile.ui.onboarding.welcome

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun WelcomeApp(
    onGetStartedClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    WelcomeScreen(
        onGetStartedClick = onGetStartedClick
    )

}