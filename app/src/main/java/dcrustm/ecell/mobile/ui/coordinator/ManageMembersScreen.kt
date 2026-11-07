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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dcrustm.ecell.mobile.domain.model.ROLE
import dcrustm.ecell.mobile.ui.directory.MemberRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageMembersScreen(
    onMemberClick: (String) -> Unit = {},
    viewModel: ManageMembersViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var query by remember { mutableStateOf("") }

    val currentUser by viewModel.currentUser.collectAsState()
    val allMembers by viewModel.allMembers.collectAsState()
    val pendingMembers by viewModel.pendingMembers.collectAsState()
    val canApprove = viewModel.canApprove
    val operationError by viewModel.operationError.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(operationError) {
        operationError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearOperationError()
        }
    }

    val filteredMembers = allMembers.filter { m ->
        query.isBlank() ||
            m.name.contains(query, ignoreCase = true) ||
            m.committee.displayName.contains(query, ignoreCase = true) ||
            m.branch.contains(query, ignoreCase = true)
    }

    val title = if (currentUser?.role == ROLE.COMMITTEE_HEAD) "My committee" else "Manage members"
    val activeTab = if (canApprove) selectedTab else 0

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(title, style = MaterialTheme.typography.titleMedium) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            if (canApprove) {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("All members")
                                Spacer(Modifier.width(4.dp))
                                CountBadge(allMembers.size)
                            }
                        })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Pending")
                                Spacer(Modifier.width(4.dp))
                                if (pendingMembers.isNotEmpty())
                                    CountBadge(pendingMembers.size, urgent = true)
                            }
                        })
                }
            }

            when (activeTab) {
                0 -> {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        placeholder = { Text("Search members…") },
                        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                    if (filteredMembers.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No members found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredMembers, key = { it.uid }) { member ->
                                MemberRow(
                                    member = member,
                                    showContact = false,
                                    onClick = { onMemberClick(member.uid) }
                                )
                            }
                        }
                    }
                }
                1 -> {
                    if (pendingMembers.isEmpty()) {
                        Box(
                            Modifier.fillMaxSize().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("No pending requests",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Medium)
                                Spacer(Modifier.height(4.dp))
                                Text("All caught up",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(pendingMembers, key = { it.uid }) { member ->
                                PendingMemberCard(
                                    member = member,
                                    onApprove = { committee -> viewModel.approve(member.uid, committee) },
                                    onReject = { viewModel.reject(member.uid) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CountBadge(count: Int, urgent: Boolean = false) {
    val bg = if (urgent) MaterialTheme.colorScheme.errorContainer
    else MaterialTheme.colorScheme.surfaceContainerHighest
    val fg = if (urgent) MaterialTheme.colorScheme.onErrorContainer
    else MaterialTheme.colorScheme.onSurfaceVariant
    Surface(shape = RoundedCornerShape(8.dp), color = bg) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
        )
    }
}
