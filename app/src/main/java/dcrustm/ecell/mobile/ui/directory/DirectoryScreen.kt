package dcrustm.ecell.mobile.ui.directory

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dcrustm.ecell.mobile.domain.model.Committee
import dcrustm.ecell.mobile.domain.model.Member
import dcrustm.ecell.mobile.domain.model.ROLE
import dcrustm.ecell.mobile.ui.home.InitialsAvatar
import dcrustm.ecell.mobile.ui.state.LoadingPanel
import dcrustm.ecell.mobile.ui.state.RetryPanel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectoryScreen(
    onMemberClick: (String) -> Unit = {},
    viewModel: DirectoryViewModel = hiltViewModel()
) {
    val memberState by viewModel.filteredState.collectAsState()
    val members = memberState.items
    var query by remember { mutableStateOf("") }
    var selectedCommittee by remember { mutableStateOf<Committee?>(null) }
    var selectedYear by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Directory", style = MaterialTheme.typography.titleMedium) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            OutlinedTextField(
                value = query,
                onValueChange = { q ->
                    query = q
                    viewModel.search(q, selectedCommittee, selectedYear)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search by name or branch…") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCommittee == null && selectedYear == null,
                        onClick = {
                            selectedCommittee = null
                            selectedYear = null
                            viewModel.search(query, null, null)
                        },
                        label = { Text("All") }
                    )
                }
                items(Committee.entries.filter { it != Committee.NONE }) { committee ->
                    FilterChip(
                        selected = selectedCommittee == committee,
                        onClick = {
                            selectedCommittee = if (selectedCommittee == committee) null else committee
                            viewModel.search(query, selectedCommittee, selectedYear)
                        },
                        label = { Text(committee.shortName) }
                    )
                }
                items(listOf(1, 2, 3, 4)) { year ->
                    FilterChip(
                        selected = selectedYear == year,
                        onClick = {
                            selectedYear = if (selectedYear == year) null else year
                            viewModel.search(query, selectedCommittee, selectedYear)
                        },
                        label = { Text("Year $year") }
                    )
                }
            }

            if (memberState.isLoading) {
                LoadingPanel(Modifier.weight(1f))
            } else if (memberState.errorMessage != null) {
                RetryPanel(
                    message = memberState.errorMessage ?: "Could not load the directory.",
                    onRetry = viewModel::retry,
                    modifier = Modifier.weight(1f)
                )
            } else if (members.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No members found", style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            "${members.size} member${if (members.size != 1) "s" else ""}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    items(members, key = { it.uid }) { member ->
                        MemberRow(
                            member = member,
                            // Contact data is loaded only on the detail screen for authorized viewers.
                            showContact = false,
                            onClick = { onMemberClick(member.uid) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MemberRow(
    member: Member,
    showContact: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            InitialsAvatar(initials = member.initials, size = 44)

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = member.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (member.role == ROLE.COMMITTEE_HEAD || member.role == ROLE.COORDINATOR) {
                        RolePill(member.role)
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${member.yearLabel} · ${member.branch} · ${member.committee.shortName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (showContact && member.phone.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = member.phone,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun RolePill(role: ROLE) {
    val (bg, fg, label) = when (role) {
        ROLE.COORDINATOR -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            "Coordinator"
        )
        ROLE.COMMITTEE_HEAD -> Triple(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
            "Head"
        )
        else -> return
    }
    Surface(shape = RoundedCornerShape(4.dp), color = bg) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
        )
    }
}
