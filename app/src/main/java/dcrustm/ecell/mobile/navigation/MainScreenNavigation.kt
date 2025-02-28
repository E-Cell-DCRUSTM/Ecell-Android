package dcrustm.ecell.mobile.navigation

import kotlinx.serialization.Serializable

sealed class MainScreenNavigation {

    @Serializable
    data object HomeAppRoute: MainScreenNavigation()

    @Serializable
    data object MeetAppRoute: MainScreenNavigation()

    @Serializable
    data object QuizAppRoute: MainScreenNavigation()

    @Serializable
    data object AdminAppRoute: MainScreenNavigation()

}

