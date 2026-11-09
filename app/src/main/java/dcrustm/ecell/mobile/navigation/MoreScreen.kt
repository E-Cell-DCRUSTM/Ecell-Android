package dcrustm.ecell.mobile.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dcrustm.ecell.mobile.domain.model.ROLE

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    role: ROLE,
    onOpenRoute: (Screen) -> Unit,
    onOpenProfile: () -> Unit,
    onOpenAbout: () -> Unit
) {
    val destinations = moreScreensForRole(role)

    Scaffold(
        topBar = { TopAppBar(title = { Text("More", style = MaterialTheme.typography.titleMedium) }) }
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(contentPadding),
        ) {
            item {
                Text(
                    "E-Cell workspace",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 20.dp, top = 8.dp, bottom = 4.dp)
                )
            }
            items(destinations, key = { it.route }) { screen ->
                ListItem(
                    headlineContent = { Text(screen.title, fontWeight = FontWeight.Medium) },
                    supportingContent = {
                        Text(destinationDescription(screen), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    leadingContent = {
                        Icon(screen.outlinedIcon, contentDescription = null)
                    },
                    modifier = Modifier.clickable { onOpenRoute(screen) }
                )
            }
            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }
            item {
                Text(
                    "Account",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 20.dp, top = 4.dp, bottom = 4.dp)
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Profile", fontWeight = FontWeight.Medium) },
                    supportingContent = { Text("Contact details and sign out") },
                    leadingContent = { Icon(Icons.Outlined.AccountCircle, contentDescription = null) },
                    modifier = Modifier.clickable(onClick = onOpenProfile)
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("About E-Cell", fontWeight = FontWeight.Medium) },
                    leadingContent = { Icon(Icons.Outlined.Info, contentDescription = null) },
                    modifier = Modifier.clickable(onClick = onOpenAbout)
                )
            }
        }
    }
}

private fun destinationDescription(screen: Screen): String = when (screen) {
    Screen.Directory -> "Browse approved E-Cell members"
    Screen.MyCommittee -> "View and search your committee roster"
    Screen.Complaints -> "Submit a private complaint"
    Screen.MyComplaints -> "Check your complaint status and coordinator responses"
    Screen.Members -> "Manage member roles and pending applications"
    Screen.Inbox -> "Review and respond to member complaints"
    else -> screen.title
}
