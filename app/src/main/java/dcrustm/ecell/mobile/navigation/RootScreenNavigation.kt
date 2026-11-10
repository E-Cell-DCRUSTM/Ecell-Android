package dcrustm.ecell.mobile.navigation

// Define the root-level destinations as type-safe objects.
sealed class RootScreen(val route: String) {
    object Main : RootScreen("main")
    object Profile : RootScreen("profile")
    object MemberId : RootScreen("member_id")
    object VerifyMember : RootScreen("verify_member")
    object Info : RootScreen("info")
    object MemberDetail : RootScreen("member_detail/{uid}") {
        fun createRoute(uid: String) = "member_detail/$uid"
    }
}
