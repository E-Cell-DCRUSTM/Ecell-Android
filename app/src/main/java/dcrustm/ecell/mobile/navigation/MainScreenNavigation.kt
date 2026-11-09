package dcrustm.ecell.mobile.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Feedback
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.HowToVote
import androidx.compose.ui.graphics.vector.ImageVector
import dcrustm.ecell.mobile.domain.model.ROLE

sealed class Screen(
    val route: String,
    val title: String,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector,
    val visibleTo: Set<ROLE> = setOf(ROLE.MEMBER, ROLE.COMMITTEE_HEAD, ROLE.COORDINATOR)
) {
    object Home : Screen(
        "home", "Home",
        Icons.Filled.Campaign, Icons.Outlined.Campaign
    )

    object Events : Screen(
        "events", "Schedule",
        Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth
    )

    object Directory : Screen(
        "directory", "Directory",
        Icons.Filled.People, Icons.Outlined.People
    )

    object MyCommittee : Screen(
        "my_committee", "My Committee",
        Icons.Filled.Group, Icons.Outlined.Group,
        visibleTo = setOf(ROLE.COMMITTEE_HEAD)
    )

    object Complaints : Screen(
        "complaints", "Complaints",
        Icons.Filled.Feedback, Icons.Outlined.Feedback,
        visibleTo = setOf(ROLE.MEMBER, ROLE.COMMITTEE_HEAD)
    )

    object Members : Screen(
        "members", "Members",
        Icons.Filled.ManageAccounts, Icons.Outlined.ManageAccounts,
        visibleTo = setOf(ROLE.COORDINATOR)
    )

    object Inbox : Screen(
        "inbox", "Inbox",
        Icons.Filled.Inbox, Icons.Outlined.Inbox,
        visibleTo = setOf(ROLE.COORDINATOR)
    )

    object Polls : Screen(
        "polls", "Voting",
        Icons.Filled.HowToVote, Icons.Outlined.HowToVote
    )

    object Messages : Screen(
        "messages", "Messages",
        Icons.Filled.ChatBubble, Icons.Outlined.ChatBubbleOutline
    )
}

fun screensForRole(role: ROLE): List<Screen> = listOf(
    Screen.Home,
    Screen.Events,
    Screen.Directory,
    Screen.MyCommittee,
    Screen.Complaints,
    Screen.Members,
    Screen.Inbox,
    Screen.Polls,
    Screen.Messages
).filter { role in it.visibleTo }
