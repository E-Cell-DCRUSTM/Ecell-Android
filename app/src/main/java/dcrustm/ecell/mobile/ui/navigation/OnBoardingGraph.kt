package dcrustm.ecell.mobile.ui.navigation

import androidx.compose.runtime.Composable
import androidx.credentials.CredentialManager
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dcrustm.ecell.mobile.ui.signin.SignInApp
import dcrustm.ecell.mobile.ui.welcome.WelcomeApp
import kotlinx.serialization.Serializable

@Serializable
object WelcomeAppRoute

@Serializable
object SignInAppRoute

@Serializable
object MainAppRoute
// TODO: Check whether I need to pass some kind of data to MainScreen route, first time using firebase I guess.

// TODO: Add navigation animation later on.

@Composable
fun OnboardingNavigation(
    navController: NavHostController = rememberNavController(),
    credentialManager: CredentialManager,
    startDestination: Any = WelcomeAppRoute
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable<WelcomeAppRoute> {
            WelcomeApp(
                onGetStartedClick = {
                    navController.navigate(SignInAppRoute)
                }
            )
        }

        composable<SignInAppRoute> {
            SignInApp(
                credentialManager = credentialManager
            )
        }

        composable<MainAppRoute> {
            // Implement the actual home screen graph
            MainGraph()
        }

    }
}