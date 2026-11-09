package dcrustm.ecell.mobile.ui.coordinator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dcrustm.ecell.mobile.domain.model.Committee
import dcrustm.ecell.mobile.domain.model.Member
import dcrustm.ecell.mobile.domain.model.ROLE

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingApprovalsScreen(viewModel: ManageMembersViewModel = hiltViewModel()) {
    val pending by viewModel.pendingMembers.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Pending approvals", style = MaterialTheme.typography.titleMedium)
                        if (pending.isNotEmpty()) {
                            Text("${pending.size} waiting", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        if (pending.isEmpty()) {
            Box(
                Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("All caught up", style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(4.dp))
                    Text("No pending requests", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding() + 8.dp,
                    bottom = 80.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(pending, key = { it.uid }) { member ->
                    PendingMemberCard(
                        member = member,
                        onApprove = { committee, role -> viewModel.approve(member.uid, member.name, committee, role) },
                        onReject = { viewModel.reject(member.uid, member.name) }
                    )
                }
            }
        }
    }
}

@Composable
internal fun PendingMemberCard(
    member: Member,
    onApprove: (Committee, ROLE) -> Unit,
    onReject: () -> Unit
) {
    var showCommitteeMenu by remember { mutableStateOf(false) }
    var showRoleMenu by remember { mutableStateOf(false) }
    var showRejectConfirmation by remember { mutableStateOf(false) }
    var assignedCommittee by remember { mutableStateOf(member.committee) }
    var assignedRole by remember { mutableStateOf(ROLE.MEMBER) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(member.name, style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(2.dp))
                    Text(member.email, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(2.dp))
                    Text("${member.yearLabel} · ${member.branch}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("Requested ${member.timeAgoJoined}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Committee: ", style = MaterialTheme.typography.labelMedium)
                Box {
                    OutlinedButton(
                        onClick = { showCommitteeMenu = true },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(assignedCommittee.shortName,
                            style = MaterialTheme.typography.labelMedium)
                    }
                    DropdownMenu(
                        expanded = showCommitteeMenu,
                        onDismissRequest = { showCommitteeMenu = false }
                    ) {
                        Committee.entries.filter { it != Committee.NONE }.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c.displayName, style = MaterialTheme.typography.bodySmall) },
                                onClick = { assignedCommittee = c; showCommitteeMenu = false }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Role: ", style = MaterialTheme.typography.labelMedium)
                Box {
                    OutlinedButton(
                        onClick = { showRoleMenu = true },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            if (assignedRole == ROLE.COMMITTEE_HEAD) "Committee head" else "Member",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    DropdownMenu(
                        expanded = showRoleMenu,
                        onDismissRequest = { showRoleMenu = false }
                    ) {
                        listOf(ROLE.MEMBER, ROLE.COMMITTEE_HEAD).forEach { role ->
                            DropdownMenuItem(
                                text = { Text(if (role == ROLE.COMMITTEE_HEAD) "Committee head" else "Member") },
                                onClick = { assignedRole = role; showRoleMenu = false }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { showRejectConfirmation = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Decline")
                }
                Button(
                    onClick = { onApprove(assignedCommittee, assignedRole) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Approve")
                }
            }
        }
    }

    if (showRejectConfirmation) {
        AlertDialog(
            onDismissRequest = { showRejectConfirmation = false },
            title = { Text("Decline ${member.name}'s request?") },
            text = {
                Text("Their pending profile and contact record will be removed. Their sign-in account stays active, so they can sign in again to submit a new request.")
            },
            confirmButton = {
                TextButton(onClick = {
                    showRejectConfirmation = false
                    onReject()
                }) { Text("Decline request") }
            },
            dismissButton = {
                TextButton(onClick = { showRejectConfirmation = false }) { Text("Keep request") }
            }
        )
    }
}

private val Member.timeAgoJoined: String get() {
    val diff = System.currentTimeMillis() - joinedAt
    val hours = diff / 3_600_000
    val days = diff / 86_400_000
    return when {
        hours < 1 -> "just now"
        hours < 24 -> "${hours}h ago"
        days == 1L -> "yesterday"
        else -> "${days}d ago"
    }
}
