package dcrustm.ecell.mobile.ui.poll

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dcrustm.ecell.mobile.domain.model.Committee
import dcrustm.ecell.mobile.domain.model.Poll
import dcrustm.ecell.mobile.domain.model.ROLE

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PollsScreen(viewModel: PollsViewModel = hiltViewModel()) {
    val polls by viewModel.polls.collectAsState()
    val user by viewModel.currentUser.collectAsState()
    val error by viewModel.error.collectAsState()
    val selectedOptions = remember { mutableStateMapOf<String, Int>() }
    var showCreate by remember { mutableStateOf(false) }
    var pollToClose by remember { mutableStateOf<Poll?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Voting", style = MaterialTheme.typography.titleMedium) }) },
        floatingActionButton = {
            if (viewModel.canCreate) {
                ExtendedFloatingActionButton(
                    onClick = { showCreate = true },
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Create poll") }
                )
            }
        }
    ) { padding ->
        if (polls.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No polls yet", style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp, top = padding.calculateTopPadding() + 8.dp,
                    bottom = padding.calculateBottomPadding() + 88.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(polls, key = { it.id }) { poll ->
                    PollCard(
                        poll = poll,
                        selectedOption = selectedOptions[poll.id],
                        canClose = viewModel.canClose(poll),
                        onSelect = { selectedOptions[poll.id] = it },
                        onVote = { selectedOptions[poll.id]?.let { viewModel.vote(poll.id, it) } },
                        onClose = { pollToClose = poll }
                    )
                }
            }
        }
    }

    if (showCreate) {
        CreatePollDialog(
            user = user,
            onDismiss = { showCreate = false },
            onCreate = { title, question, options, audience ->
                viewModel.clearError()
                viewModel.create(title, question, options, audience) { showCreate = false }
            }
        )
    }
    pollToClose?.let { poll ->
        AlertDialog(
            onDismissRequest = { pollToClose = null },
            title = { Text("Close this poll?") },
            text = { Text("Members will no longer be able to vote, and the results will become visible. Other members cannot see who chose each option.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.close(poll.id)
                    pollToClose = null
                }) { Text("Close poll") }
            },
            dismissButton = { TextButton(onClick = { pollToClose = null }) { Text("Keep open") } }
        )
    }
    error?.let { message ->
        AlertDialog(
            onDismissRequest = viewModel::clearError,
            title = { Text("Could not complete that action") },
            text = { Text(message) },
            confirmButton = { TextButton(onClick = viewModel::clearError) { Text("OK") } }
        )
    }
}

@Composable
private fun PollCard(
    poll: Poll,
    selectedOption: Int?,
    canClose: Boolean,
    onSelect: (Int) -> Unit,
    onVote: () -> Unit,
    onClose: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (poll.isOpen) "OPEN" else "CLOSED",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                Text(if (poll.isOrgWide) "All members" else Committee.values()
                    .firstOrNull { it.name == poll.audienceCommittee }?.displayName ?: "Committee",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(8.dp))
            Text(poll.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(poll.question, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(10.dp))

            if (poll.isOpen) {
                if (poll.hasVoted) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Lock, contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(6.dp))
                        Text("Vote recorded. Results stay hidden until the poll closes.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    poll.options.forEachIndexed { index, option ->
                        Row(verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()) {
                            RadioButton(selected = selectedOption == index, onClick = { onSelect(index) })
                            Text(option, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    Button(onClick = onVote, enabled = selectedOption != null) {
                        Text("Submit vote")
                    }
                }
                if (canClose) {
                    TextButton(onClick = onClose) { Text("Close poll and reveal results") }
                }
            } else {
                Text("${poll.totalVotes} vote${if (poll.totalVotes == 1) "" else "s"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                poll.options.forEachIndexed { index, option ->
                    val votes = poll.tallies.getOrElse(index) { 0 }
                    val fraction = if (poll.totalVotes == 0) 0f else votes.toFloat() / poll.totalVotes
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(option, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                        Text("$votes  ${"%.0f".format(fraction * 100)}%",
                            style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(Modifier.height(3.dp))
                    LinearProgressIndicator(progress = { fraction }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun CreatePollDialog(
    user: dcrustm.ecell.mobile.domain.model.Member?,
    onDismiss: () -> Unit,
    onCreate: (String, String, List<String>, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var question by remember { mutableStateOf("") }
    var choices by remember { mutableStateOf("") }
    var audience by remember {
        mutableStateOf(if (user?.role == ROLE.COMMITTEE_HEAD) user.committee.name else "ALL")
    }
    var menuOpen by remember { mutableStateOf(false) }
    val committeeTargets = if (user?.role == ROLE.COORDINATOR) {
        listOf("ALL") + Committee.values().filter { it != Committee.NONE }.map { it.name }
    } else listOf(user?.committee?.name ?: "ALL")
    val options = choices.lines().map(String::trim).filter(String::isNotEmpty).distinct()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create a poll") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Approved members can vote once. Other members cannot see who chose each option, and results stay hidden until you close the poll.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(title, { title = it }, label = { Text("Title") }, singleLine = true)
                OutlinedTextField(question, { question = it }, label = { Text("Question") })
                OutlinedTextField(
                    choices, { choices = it }, label = { Text("Options (one per line)") },
                    minLines = 3, supportingText = { Text("Add 2 to 8 choices") }
                )
                Box {
                    OutlinedButton(onClick = { menuOpen = true }) {
                        Text(if (audience == "ALL") "Audience: All members" else
                            "Audience: ${Committee.values().firstOrNull { it.name == audience }?.displayName ?: audience}")
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        committeeTargets.forEach { target ->
                            DropdownMenuItem(
                                text = { Text(if (target == "ALL") "All members" else
                                    Committee.values().firstOrNull { it.name == target }?.displayName ?: target) },
                                onClick = { audience = target; menuOpen = false }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank() && question.isNotBlank() && options.size in 2..8,
                onClick = { onCreate(title, question, options, audience) }
            ) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
