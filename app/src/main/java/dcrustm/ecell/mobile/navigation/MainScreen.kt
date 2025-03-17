package dcrustm.ecell.mobile.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dcrustm.ecell.mobile.R
import dcrustm.ecell.mobile.ui.admin.AdminScreen
import dcrustm.ecell.mobile.ui.gallery.GalleryScreen


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(rootNavController: NavHostController) {
    val navController = rememberNavController()
    // Observe the current back stack entry to change the top app bar title.
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route ?: Screen.Dashboard.route

    // Determine the current active screen.
    val currentScreen = when (currentRoute) {
        Screen.Dashboard.route -> Screen.Dashboard
        Screen.Blogs.route -> Screen.Blogs
        Screen.Gallery.route -> Screen.Gallery
        Screen.Admin.route -> Screen.Admin
        else -> Screen.Dashboard
    }
    // List of screens for the bottom navigation.
    val screens = listOf(
        Screen.Dashboard,
        Screen.Blogs,
        Screen.Gallery,
        Screen.Admin
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = currentScreen.title,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        rootNavController.navigate(RootScreen.Profile.route)
                    }
                    ) {
                        Image(
                            painter = painterResource(R.drawable.profile),
                            contentDescription = "Profile image",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        rootNavController.navigate(RootScreen.Info.route)
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "Info icon button"
                        )
                    }
                },
                modifier = Modifier.statusBarsPadding()
            )
        },
        bottomBar = {
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
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (selected) screen.filledIcon else screen.outlinedIcon,
                                contentDescription = screen.title,
                                tint = Color(0xFF3E4246)
                            )
                        },
                        // Show only icons in the bottom nav, as the top app bar displays the title.
                        alwaysShowLabel = false
                    )
                }
            }
        },
        content = { innerPadding ->
            // Display content for the current selected screen.
            NavHost(
                navController = navController,
                startDestination = Screen.Dashboard.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Dashboard.route) { DashboardScreen() }
                composable(Screen.Blogs.route) { BlogsScreen() }
                composable(Screen.Gallery.route) { GalleryScreen() }
                composable(Screen.Admin.route) { AdminScreen() }
            }
        }
    )
}

// Dummy placeholder screens for a time while.

@Composable
fun DashboardScreen() {
    Surface(modifier = Modifier.padding(16.dp)) {
        Text(text = "Dashboard Content", style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun BlogsScreen() {
    Surface(modifier = Modifier.padding(16.dp)) {
        Text(text = "Blogs Content", style = MaterialTheme.typography.bodyLarge)
    }
}