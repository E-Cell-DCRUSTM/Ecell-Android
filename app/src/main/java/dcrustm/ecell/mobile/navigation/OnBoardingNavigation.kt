package dcrustm.ecell.mobile.navigation

import androidx.compose.runtime.Composable
import androidx.credentials.CredentialManager
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dcrustm.ecell.mobile.ui.onboarding.signin.SignInApp
import dcrustm.ecell.mobile.ui.onboarding.welcome.WelcomeApp
import kotlinx.serialization.Serializable


sealed class OnBoardingRoutes {

    @Serializable
    data object WelcomeAppRoute: OnBoardingRoutes()

    @Serializable
    data object SignInAppRoute: OnBoardingRoutes()

    @Serializable
    data object MainAppRoute: OnBoardingRoutes()

}

// TODO: Check whether I need to pass some kind of data to MainScreen route, first time using firebase I guess.

// TODO: Add navigation animation later on.

@Composable
fun OnBoardingNavigation(
    navController: NavHostController = rememberNavController(),
    credentialManager: CredentialManager,
    startDestination: Any = OnBoardingRoutes.WelcomeAppRoute
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        composable<OnBoardingRoutes.WelcomeAppRoute> {
            WelcomeApp(
                onGetStartedClick = {
                    navController.navigate(OnBoardingRoutes.SignInAppRoute)
                }
            )
        }

        composable<OnBoardingRoutes.SignInAppRoute> {
            SignInApp(
                credentialManager = credentialManager
            )
        }

        composable<OnBoardingRoutes.MainAppRoute> {

        }

    }
}