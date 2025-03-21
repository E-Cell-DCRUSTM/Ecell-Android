package dcrustm.ecell.mobile.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.credentials.CredentialManager
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dcrustm.ecell.mobile.domain.usecase.SetOnBoardingCompleteUseCase
import dcrustm.ecell.mobile.ui.onboarding.signin.SignInApp
import dcrustm.ecell.mobile.ui.onboarding.welcome.WelcomeApp
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable


sealed class OnBoardingRoutes {

    @Serializable
    data object WelcomeAppRoute: OnBoardingRoutes()

    @Serializable
    data object SignInAppRoute: OnBoardingRoutes()

    @Serializable
    data object RootAppRoute: OnBoardingRoutes()

}

// TODO: Check whether I need to pass some kind of data to MainScreen route, first time using firebase I guess.

// TODO: Add navigation animation later on.

@Composable
fun OnBoardingNavigation(
    navController: NavHostController = rememberNavController(),
    credentialManager: CredentialManager,
    setOnBoardingCompleteUseCase: SetOnBoardingCompleteUseCase,
    startDestination: Any = OnBoardingRoutes.WelcomeAppRoute
) {

    val coroutineScope = rememberCoroutineScope()


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
                credentialManager = credentialManager,
                onSuccessfulLoginEvent = {
                    navController.navigate(OnBoardingRoutes.RootAppRoute) {
                        popUpTo(OnBoardingRoutes.SignInAppRoute) {
                            inclusive = true
                        }
                    }

                    // Fire and forget the suspend call in a non-blocking way.
                    coroutineScope.launch {
                        setOnBoardingCompleteUseCase(true)
                    }
                }
            )
        }

        composable<OnBoardingRoutes.RootAppRoute> {
            RootApp()
        }
    }
}