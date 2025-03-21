package dcrustm.ecell.mobile.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import dcrustm.ecell.mobile.ui.about.AboutUsScreen
import dcrustm.ecell.mobile.ui.profile.ProfileScreen

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun RootApp() {
    // Use the animated nav controller from accompanist for custom transitions.
    val rootNavController = rememberNavController()

    AnimatedNavHost(
        navController = rootNavController,
        startDestination = RootScreen.Main.route
    ) {
        // Main route: contains your bottom navigation scaffold.
        composable(route = RootScreen.Main.route) {
            MainScreen(rootNavController = rootNavController)
        }

        // Profile route: full screen modal with custom transitions.
        composable(
            route = RootScreen.Profile.route,
            enterTransition = {
                // Slide in from bottom (rising effect)
                slideInVertically(
                    initialOffsetY = { fullHeight -> fullHeight },
                    animationSpec = tween(durationMillis = 300)
                )
            },
            exitTransition = {
                // Slide out downwards (fast shrinking)
                slideOutVertically(
                    targetOffsetY = { fullHeight -> fullHeight },
                    animationSpec = tween(durationMillis = 150)
                )
            },
            popEnterTransition = {
                slideInVertically(
                    initialOffsetY = { fullHeight -> fullHeight },
                    animationSpec = tween(durationMillis = 300)
                )
            },
            popExitTransition = {
                slideOutVertically(
                    targetOffsetY = { fullHeight -> fullHeight },
                    animationSpec = tween(durationMillis = 150)
                )
            }
        ) {
            ProfileScreen(onDismiss = { rootNavController.popBackStack() })
        }
        // Info route: full screen modal with similar custom transitions.
        composable(
            route = RootScreen.Info.route,
            enterTransition = {
                slideInVertically(
                    initialOffsetY = { fullHeight -> fullHeight },
                    animationSpec = tween(durationMillis = 300)
                )
            },
            exitTransition = {
                slideOutVertically(
                    targetOffsetY = { fullHeight -> fullHeight },
                    animationSpec = tween(durationMillis = 150)
                )
            },
            popEnterTransition = {
                slideInVertically(
                    initialOffsetY = { fullHeight -> fullHeight },
                    animationSpec = tween(durationMillis = 300)
                )
            },
            popExitTransition = {
                slideOutVertically(
                    targetOffsetY = { fullHeight -> fullHeight },
                    animationSpec = tween(durationMillis = 150)
                )
            }
        ) {
            // Assume that InfoScreen is implemented elsewhere.
            AboutUsScreen(onDismiss = { rootNavController.popBackStack() })
        }
    }
}


// Dummy profile screen for now
// TODO: Implement real Profile Screen asap.

//@Composable
//fun ProfileScreen(onDismiss: () -> Unit) {
//    Surface(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//    ) {
//        Column {
//            Text(
//                text = "Profile Screen",
//                style = MaterialTheme.typography.titleLarge,
//                modifier = Modifier.padding(bottom = 16.dp)
//            )
//            Button(onClick = onDismiss) {
//                Text("Close")
//            }
//        }
//    }
//}