package dcrustm.ecell.mobile.ui.complaint

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplaintScreen(
    onBack: () -> Unit = {},
    onSubmitted: () -> Unit = {},
    viewModel: ComplaintViewModel = hiltViewModel()
) {
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var titleError by remember { mutableStateOf("") }
    var bodyError by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("File a complaint", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (submitted) {
            SubmittedState(modifier = Modifier.padding(innerPadding))
        } else {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(Modifier.height(8.dp))

                Text(
                    "Your complaint is private. Only the coordinator can see who submitted it.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(20.dp))

                Text("Title", style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; titleError = "" },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Brief summary of the issue") },
                    singleLine = true,
                    isError = titleError.isNotBlank(),
                    supportingText = if (titleError.isNotBlank()) {
                        { Text(titleError, color = MaterialTheme.colorScheme.error) }
                    } else null,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(Modifier.height(16.dp))

                Text("Description", style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it; bodyError = "" },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    placeholder = { Text("Describe the issue in detail. Be specific about what happened, when, and who was involved.") },
                    isError = bodyError.isNotBlank(),
                    supportingText = if (bodyError.isNotBlank()) {
                        { Text(bodyError, color = MaterialTheme.colorScheme.error) }
                    } else null,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(Modifier.height(28.dp))

                Button(
                    onClick = {
                        var valid = true
                        if (title.isBlank()) { titleError = "Title is required"; valid = false }
                        if (body.isBlank()) { bodyError = "Description is required"; valid = false }
                        if (body.length < 20) { bodyError = "Please provide more detail"; valid = false }
                        if (valid) {
                            viewModel.submit(title, body) {
                                submitted = true
                                onSubmitted()
                                scope.launch {
                                    snackbarHostState.showSnackbar("Complaint submitted")
                                }
                            }
                        }
                    },
                    enabled = !isSubmitting,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isSubmitting) CircularProgressIndicator()
                    else Text("Submit complaint")
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SubmittedState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        Text("Complaint submitted", style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text(
            "The coordinator has been notified. You'll receive a response when it's reviewed.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
