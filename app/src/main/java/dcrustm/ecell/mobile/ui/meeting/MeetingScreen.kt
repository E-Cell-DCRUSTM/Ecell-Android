package dcrustm.ecell.mobile.ui.meeting


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingApp() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Meeting Panel", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = { /* TODO: Implement dismiss action */ }) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_close_clear_cancel),
                            contentDescription = "Dismiss"
                        )
                    }
                },
                modifier = Modifier.systemBarsPadding()
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            MeetingScreen()
        }
    }
}


@OptIn(ExperimentalPagerApi::class)
@Composable
fun MeetingScreen() {
    // Pager state to manage HorizontalPager
    val pagerState = rememberPagerState(initialPage = 0)
    val coroutineScope = rememberCoroutineScope()

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {

            Text(
                text = "Host Meeting",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp),
                textAlign = TextAlign.Center
            )

            // Row for clickable texts to choose meeting type
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val options = listOf("Physical", "Virtual")
                options.forEachIndexed { index, option ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            // Scroll to page when option is tapped
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        }
                    ) {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (pagerState.currentPage == index)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onBackground
                        )
                        if (pagerState.currentPage == index) {
                            Divider(
                                modifier = Modifier
                                    .height(2.dp)
                                    .width(50.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Spacer(modifier = Modifier.height(2.dp))
                        }
                    }
                }
            }

            // Divider right below the options row
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Horizontal Pager with 2 pages
            HorizontalPager(
                count = 2,
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                when (page) {
                    0 -> PhysicalMeetingContent()
                    1 -> VirtualMeetingContent()
                }
            }

            // (Optional) You may add common bottom content here if needed.
        }
    }
}

@Composable
fun PhysicalMeetingContent() {
    // Local state for each input field
    val hostName = remember { mutableStateOf("") }
    val meetingAgenda = remember { mutableStateOf("") }
    val meetingAvenue = remember { mutableStateOf("") }
    val meetingDate = remember { mutableStateOf("") }  // Will trigger date picker (maxkeppeler)
    val meetingTime = remember { mutableStateOf("") }  // Will trigger time picker (maxkeppeler)
    val notifyUsers = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Host Name
        OutlinedTextField(
            value = hostName.value,
            onValueChange = { hostName.value = it },
            label = { Text("Host Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Meeting Agenda
        OutlinedTextField(
            value = meetingAgenda.value,
            onValueChange = { meetingAgenda.value = it },
            label = { Text("Meeting Agenda") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Meeting Avenue
        OutlinedTextField(
            value = meetingAvenue.value,
            onValueChange = { meetingAvenue.value = it },
            label = { Text("Meeting Avenue") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Meeting Date Picker
        OutlinedTextField(
            value = meetingDate.value,
            onValueChange = { /* update if you change manually, otherwise open date picker dialog */ },
            label = { Text("Select Date") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    // Hook up maxkeppeler date picker here.
                },
            readOnly = true
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Meeting Time Picker
        OutlinedTextField(
            value = meetingTime.value,
            onValueChange = { /* update if you change manually, otherwise open time picker dialog */ },
            label = { Text("Select Time") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    // Hook up maxkeppeler time picker here.
                },
            readOnly = true
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Switch for user notification
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Should users be notified?")
            Spacer(modifier = Modifier.weight(1f))
            Switch(
                checked = notifyUsers.value,
                onCheckedChange = { notifyUsers.value = it }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Host and Dismiss buttons at the bottom of the form
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { /* Host meeting action */ }) {
                Text(text = "Host")
            }
            Button(onClick = { /* Dismiss meeting action */ }) {
                Text(text = "Dismiss")
            }
        }
    }
}


@Composable
fun VirtualMeetingContent() {
    // Local state for each input field
    val hostName = remember { mutableStateOf("") }
    val meetingAgenda = remember { mutableStateOf("") }
    val meetingUrl = remember { mutableStateOf("") }
    val meetingDate = remember { mutableStateOf("") }  // Will trigger date picker (maxkeppeler)
    val meetingTime = remember { mutableStateOf("") }  // Will trigger time picker (maxkeppeler)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(8.dp)
    ) {
        // Host Name
        OutlinedTextField(
            value = hostName.value,
            onValueChange = { hostName.value = it },
            label = { Text("Host Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Meeting Agenda
        OutlinedTextField(
            value = meetingAgenda.value,
            onValueChange = { meetingAgenda.value = it },
            label = { Text("Meeting Agenda") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Meeting Url
        OutlinedTextField(
            value = meetingUrl.value,
            onValueChange = { meetingUrl.value = it },
            label = { Text("Meeting Url") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Meeting Date Picker
        OutlinedTextField(
            value = meetingDate.value,
            onValueChange = { /* Optional: update manually if needed */ },
            label = { Text("Select Date") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    // Trigger the maxkeppeler date picker here.
                },
            readOnly = true
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Meeting Time Picker
        OutlinedTextField(
            value = meetingTime.value,
            onValueChange = { /* Optional: update manually if needed */ },
            label = { Text("Select Time") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    // Trigger the maxkeppeler time picker here.
                },
            readOnly = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Host and Dismiss buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { /* Host meeting action */ }) {
                Text("Host")
            }
            Button(onClick = { /* Dismiss meeting action */ }) {
                Text("Dismiss")
            }
        }
    }
}