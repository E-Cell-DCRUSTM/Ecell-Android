package dcrustm.ecell.mobile.ui.complaint

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dcrustm.ecell.mobile.domain.model.Complaint
import dcrustm.ecell.mobile.ui.state.ListUiState
import dcrustm.ecell.mobile.ui.state.LoadingPanel
import dcrustm.ecell.mobile.ui.state.RetryPanel

@Composable
fun MyComplaintsScreen(
    onBack: () -> Unit,
    onFileComplaint: () -> Unit,
    viewModel: MyComplaintsViewModel = hiltViewModel()
) {
    val state by viewModel.complaintState.collectAsState()
    MyComplaintsContent(
        state = state,
        onBack = onBack,
        onRetry = viewModel::retry,
        onFileComplaint = onFileComplaint
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyComplaintsContent(
    state: ListUiState<Complaint>,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onFileComplaint: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Complaints", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            state.isLoading -> LoadingPanel(Modifier.padding(innerPadding))
            state.errorMessage != null -> RetryPanel(
                message = state.errorMessage,
                onRetry = onRetry,
                modifier = Modifier.padding(innerPadding)
            )
            state.items.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No complaints yet", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Complaints you submit and their coordinator responses will appear here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onFileComplaint) { Text("File a complaint") }
                }
            }
            else -> LazyColumn(
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding() + 8.dp,
                    bottom = innerPadding.calculateBottomPadding() + 16.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.items, key = { it.id }) { complaint ->
                    OwnComplaintCard(complaint)
                }
            }
        }
    }
}

@Composable
private fun OwnComplaintCard(complaint: Complaint) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(complaint.status.label, style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text(complaint.title, style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text(complaint.body, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
            Text(complaint.timeAgo, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            if (complaint.response.isBlank()) {
                Text(
                    "Waiting for a coordinator response.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text("Coordinator response", style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text(complaint.response, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
