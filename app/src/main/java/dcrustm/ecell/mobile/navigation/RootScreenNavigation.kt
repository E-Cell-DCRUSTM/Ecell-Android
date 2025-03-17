package dcrustm.ecell.mobile.navigation

// Define the root-level destinations as type-safe objects.
sealed class RootScreen(val route: String) {
    object Main : RootScreen("main")
    object Profile : RootScreen("profile")
    object Info : RootScreen("info")
}