package dcrustm.ecell.mobile.ui.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
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
import dcrustm.ecell.mobile.domain.model.Conversation
import dcrustm.ecell.mobile.domain.model.Member
import dcrustm.ecell.mobile.ui.home.InitialsAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(
    onOpenConversation: (String, String) -> Unit,
    viewModel: MessagesViewModel = hiltViewModel()
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val members by viewModel.approvedMembers.collectAsState()
    val conversations by viewModel.conversations.collectAsState()
    val error by viewModel.error.collectAsState()
    var showMembers by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Messages", style = MaterialTheme.typography.titleMedium) }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showMembers = true },
                icon = { androidx.compose.material3.Icon(Icons.Filled.Edit, contentDescription = null) },
                text = { Text("New message") }
            )
        }
    ) { padding ->
        if (conversations.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Start a private chat with an E-Cell member",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(conversations, key = { it.id }) { conversation ->
                    ConversationRow(
                        conversation = conversation,
                        myUid = currentUser?.uid.orEmpty(),
                        member = members.firstOrNull { it.uid == conversation.otherUid(currentUser?.uid.orEmpty()) },
                        onClick = { onOpenConversation(conversation.id, conversation.otherUid(currentUser?.uid.orEmpty())) }
                    )
                }
            }
        }
    }

    if (showMembers) {
        MemberPickerDialog(
            members = members.filter { it.uid != currentUser?.uid },
            onDismiss = { showMembers = false },
            onSelect = { member ->
                showMembers = false
                viewModel.openChat(member) { id -> onOpenConversation(id, member.uid) }
            }
        )
    }
    error?.let { message ->
        AlertDialog(
            onDismissRequest = viewModel::clearError,
            title = { Text("Message could not open") },
            text = { Text(message) },
            confirmButton = { TextButton(onClick = viewModel::clearError) { Text("OK") } }
        )
    }
}

@Composable
private fun ConversationRow(
    conversation: Conversation,
    myUid: String,
    member: Member?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            InitialsAvatar(member?.initials ?: "?", 44)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(member?.name ?: "E-Cell member", fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(3.dp))
                Text(
                    if (conversation.lastSenderUid == myUid && conversation.lastMessage.isNotBlank())
                        "You: ${conversation.lastMessage}" else conversation.lastMessage.ifBlank { "Start chatting" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun MemberPickerDialog(
    members: List<Member>,
    onDismiss: () -> Unit,
    onSelect: (Member) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = members.filter { it.name.contains(query, ignoreCase = true) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New message") },
        text = {
            Column(Modifier.fillMaxWidth()) {
                OutlinedTextField(query, { query = it }, label = { Text("Find a member") }, singleLine = true)
                Spacer(Modifier.height(8.dp))
                if (members.isEmpty()) {
                    Text("No other approved members are available yet.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else if (filtered.isEmpty()) {
                    Text("No matching member", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    LazyColumn(Modifier.height(300.dp)) {
                        items(filtered, key = { it.uid }) { member ->
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable { onSelect(member) }.padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                InitialsAvatar(member.initials, 36)
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(member.name, fontWeight = FontWeight.Medium)
                                    Text(member.committee.displayName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}
