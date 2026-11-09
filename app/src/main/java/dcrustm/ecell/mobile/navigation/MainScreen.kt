package dcrustm.ecell.mobile.navigation

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dcrustm.ecell.mobile.domain.model.ROLE
import dcrustm.ecell.mobile.ui.complaint.ComplaintScreen
import dcrustm.ecell.mobile.ui.complaint.ComplaintInboxScreen
import dcrustm.ecell.mobile.ui.coordinator.ManageMembersScreen
import dcrustm.ecell.mobile.ui.chat.ChatScreen
import dcrustm.ecell.mobile.ui.chat.MessagesScreen
import dcrustm.ecell.mobile.ui.directory.DirectoryScreen
import dcrustm.ecell.mobile.ui.events.CreateEventScreen
import dcrustm.ecell.mobile.ui.events.EventsScreen
import dcrustm.ecell.mobile.ui.home.HomeScreen
import dcrustm.ecell.mobile.ui.home.InitialsAvatar
import dcrustm.ecell.mobile.ui.home.PostAnnouncementScreen
import dcrustm.ecell.mobile.ui.poll.PollsScreen

private const val ROUTE_POST_ANNOUNCEMENT = "post_announcement"
private const val ROUTE_CREATE_EVENT = "create_event"
private const val ROUTE_CHAT = "chat/{conversationId}/{otherUid}"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    rootNavController: NavHostController,
    viewModel: MainViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val currentUser by viewModel.currentUser.collectAsState()
    val role = currentUser?.role ?: ROLE.PENDING
    val screens = screensForRole(role)

    if (screens.isEmpty()) {
        PendingApprovalScreen(onSignOut = { viewModel.signOut() })
        return
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Either way, push just won't show locally — nothing else to do here. */ }

    LaunchedEffect(currentUser?.uid) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        viewModel.registerPushToken()
    }

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route ?: Screen.Home.route
    val currentScreen = screens.firstOrNull { it.route == currentRoute } ?: screens.first()
    val isChatRoute = currentRoute?.startsWith("chat/") == true

    Scaffold(
        topBar = {
            if (!isChatRoute) {
                CenterAlignedTopAppBar(
                    title = { Text(text = currentScreen.title, style = MaterialTheme.typography.titleMedium) },
                    navigationIcon = {
                        IconButton(onClick = { rootNavController.navigate(RootScreen.Profile.route) }) {
                            Box(contentAlignment = Alignment.Center) {
                                InitialsAvatar(initials = currentUser?.initials ?: "?", size = 34)
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { rootNavController.navigate(RootScreen.Info.route) }) {
                            Icon(Icons.Outlined.Info, contentDescription = "About")
                        }
                    },
                    modifier = Modifier.statusBarsPadding()
                )
            }
        },
        bottomBar = {
            if (!isChatRoute) {
                NavigationBar(
                    containerColor = Color(0xFFFCFCF9),
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    screens.forEach { screen ->
                        val selected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) screen.filledIcon else screen.outlinedIcon,
                                    contentDescription = screen.title
                                )
                            },
                            alwaysShowLabel = false
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(onPostClick = { navController.navigate(ROUTE_POST_ANNOUNCEMENT) })
            }
            composable(Screen.Events.route) {
                EventsScreen(onCreateClick = { navController.navigate(ROUTE_CREATE_EVENT) })
            }
            composable(Screen.Directory.route) {
                DirectoryScreen(
                    onMemberClick = { uid ->
                        rootNavController.navigate(RootScreen.MemberDetail.createRoute(uid))
                    }
                )
            }
            composable(Screen.MyCommittee.route) {
                ManageMembersScreen(
                    onMemberClick = { uid ->
                        rootNavController.navigate(RootScreen.MemberDetail.createRoute(uid))
                    }
                )
            }
            composable(Screen.Complaints.route) {
                ComplaintScreen()
            }
            composable(Screen.Members.route) {
                ManageMembersScreen(
                    onMemberClick = { uid ->
                        rootNavController.navigate(RootScreen.MemberDetail.createRoute(uid))
                    }
                )
            }
            composable(Screen.Inbox.route) {
                ComplaintInboxScreen()
            }
            composable(Screen.Polls.route) {
                PollsScreen()
            }
            composable(Screen.Messages.route) {
                MessagesScreen(onOpenConversation = { conversationId, otherUid ->
                    navController.navigate("chat/$conversationId/$otherUid")
                })
            }
            composable(ROUTE_CHAT) {
                ChatScreen(onBack = { navController.popBackStack() })
            }
            composable(ROUTE_POST_ANNOUNCEMENT) {
                PostAnnouncementScreen(onBack = { navController.popBackStack() })
            }
            composable(ROUTE_CREATE_EVENT) {
                CreateEventScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
