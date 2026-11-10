package dcrustm.ecell.mobile.ui.coordinator

import android.graphics.Bitmap
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dcrustm.ecell.mobile.domain.model.Committee
import dcrustm.ecell.mobile.domain.model.PendingMemberApplication
import dcrustm.ecell.mobile.domain.model.ROLE
import dcrustm.ecell.mobile.ui.home.InitialsAvatar
import dcrustm.ecell.mobile.ui.state.LoadingPanel
import dcrustm.ecell.mobile.ui.state.RetryPanel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingApprovalsScreen(viewModel: ManageMembersViewModel = hiltViewModel()) {
    val state by viewModel.pendingState.collectAsState()
    val photos by viewModel.pendingPhotos.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Pending approvals", style = MaterialTheme.typography.titleMedium)
                        if (state.items.isNotEmpty()) {
                            Text("${state.items.size} waiting", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        when {
            state.isLoading -> LoadingPanel(Modifier.fillMaxSize().padding(innerPadding))
            state.errorMessage != null -> RetryPanel(
                state.errorMessage ?: "Could not load approval requests.", viewModel::retry,
                Modifier.fillMaxSize().padding(innerPadding)
            )
            state.items.isEmpty() -> Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("All caught up", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(4.dp))
                    Text("No pending requests", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            else -> LazyColumn(
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding() + 8.dp,
                    bottom = 80.dp, start = 16.dp, end = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.items, key = { it.member.uid }) { application ->
                    PendingMemberCard(
                        application = application,
                        photo = photos[application.member.uid],
                        onApprove = { committee, role -> viewModel.approve(application.member.uid, application.member.name, committee, role) },
                        onReject = { message -> viewModel.reject(application.member.uid, application.member.name, message) },
                        onCorrection = { message -> viewModel.requestCorrection(application.member.uid, application.member.name, message) }
                    )
                }
            }
        }
    }
}

@Composable
internal fun PendingMemberCard(
    application: PendingMemberApplication,
    photo: Bitmap?,
    onApprove: (Committee, ROLE) -> Unit,
    onReject: (String) -> Unit,
    onCorrection: (String) -> Unit
) {
    val member = application.member
    var showCommitteeMenu by remember(member.uid) { mutableStateOf(false) }
    var showRoleMenu by remember(member.uid) { mutableStateOf(false) }
    var showApprovalConfirmation by remember(member.uid) { mutableStateOf(false) }
    var actionDialog by remember(member.uid) { mutableStateOf<ReviewAction?>(null) }
    var assignedCommittee by remember(member.uid) {
        mutableStateOf(application.application.preferredCommittee.takeIf { it != Committee.NONE } ?: member.committee)
    }
    var assignedRole by remember(member.uid) { mutableStateOf(ROLE.MEMBER) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (photo != null) {
                    Image(photo.asImageBitmap(), contentDescription = "${member.name}'s profile photo",
                        modifier = Modifier.size(56.dp))
                } else {
                    InitialsAvatar(member.initials, size = 56)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(member.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(member.email, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Submitted ${member.timeAgoJoined}", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text("${member.yearLabel} · ${member.branch}${application.application.section.takeIf { it.isNotBlank() }?.let { " · Section $it" }.orEmpty()}",
                style = MaterialTheme.typography.bodyMedium)
            if (application.application.enrollmentNumber.isNotBlank()) {
                DetailLine("Enrollment / roll number", application.application.enrollmentNumber)
            }
            DetailLine("Preferred committee", application.application.preferredCommittee.displayName)
            if (member.phone.isNotBlank()) DetailLine("Contact number", member.phone)
            if (member.bio.isNotBlank()) DetailLine("Introduction", member.bio)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Assign committee", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.width(8.dp))
                Box {
                    OutlinedButton(onClick = { showCommitteeMenu = true }) { Text(assignedCommittee.displayName) }
                    DropdownMenu(expanded = showCommitteeMenu, onDismissRequest = { showCommitteeMenu = false }) {
                        Committee.entries.filter { it != Committee.NONE }.forEach { committee ->
                            DropdownMenuItem(text = { Text(committee.displayName) }, onClick = {
                                assignedCommittee = committee
                                showCommitteeMenu = false
                            })
                        }
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Role", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.width(8.dp))
                Box {
                    OutlinedButton(onClick = { showRoleMenu = true }) {
                        Text(if (assignedRole == ROLE.COMMITTEE_HEAD) "Committee head" else "Member")
                    }
                    DropdownMenu(expanded = showRoleMenu, onDismissRequest = { showRoleMenu = false }) {
                        listOf(ROLE.MEMBER, ROLE.COMMITTEE_HEAD).forEach { role ->
                            DropdownMenuItem(text = { Text(if (role == ROLE.COMMITTEE_HEAD) "Committee head" else "Member") },
                                onClick = { assignedRole = role; showRoleMenu = false })
                        }
                    }
                }
            }
            Text("A unique E-Cell Member ID is created automatically when you approve.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (assignedCommittee == Committee.NONE) {
                Text("Choose a committee before approving this member.",
                    color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            Button(
                onClick = { showApprovalConfirmation = true },
                enabled = assignedCommittee != Committee.NONE,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Approve application")
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = { actionDialog = ReviewAction.CORRECTION }) { Text("Request correction") }
                TextButton(onClick = { actionDialog = ReviewAction.REJECT }) {
                    Text("Reject", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    if (showApprovalConfirmation) {
        val roleName = if (assignedRole == ROLE.COMMITTEE_HEAD) "committee head" else "member"
        AlertDialog(
            onDismissRequest = { showApprovalConfirmation = false },
            title = { Text("Approve E-Cell membership?") },
            text = {
                Text("Activate ${member.name}'s membership as a $roleName in ${assignedCommittee.displayName}? A unique E-Cell Member ID will be created.")
            },
            confirmButton = {
                TextButton(onClick = {
                    showApprovalConfirmation = false
                    onApprove(assignedCommittee, assignedRole)
                }) { Text("Approve member") }
            },
            dismissButton = {
                TextButton(onClick = { showApprovalConfirmation = false }) { Text("Cancel") }
            }
        )
    }

    actionDialog?.let { action ->
        ReviewFeedbackDialog(
            action = action,
            memberName = member.name,
            onDismiss = { actionDialog = null },
            onConfirm = { message ->
                actionDialog = null
                if (action == ReviewAction.REJECT) onReject(message) else onCorrection(message)
            }
        )
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("$label:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        Text(value, style = MaterialTheme.typography.bodySmall)
    }
}

private enum class ReviewAction { CORRECTION, REJECT }

@Composable
private fun ReviewFeedbackDialog(
    action: ReviewAction,
    memberName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var message by remember(action, memberName) { mutableStateOf("") }
    val correction = action == ReviewAction.CORRECTION
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (correction) "Request profile correction" else "Reject application") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(if (correction) "Explain what $memberName should update. They can edit the saved profile and resubmit."
                    else "The application will be retained as rejected. The member can review the note and reapply.")
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it.take(500) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(if (correction) "Correction needed" else "Note (optional)") },
                    supportingText = { Text("${message.length}/500") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(message.trim()) }, enabled = !correction || message.isNotBlank()) {
                Text(if (correction) "Send request" else "Reject application")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private val dcrustm.ecell.mobile.domain.model.Member.timeAgoJoined: String get() {
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
