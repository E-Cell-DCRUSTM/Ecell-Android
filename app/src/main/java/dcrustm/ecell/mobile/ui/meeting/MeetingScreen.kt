package dcrustm.ecell.mobile.ui.meeting

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

// Define the meeting data model
sealed class Meeting {
    abstract val topic: String
    abstract val scheduleTime: String
    abstract val hostName: String

    data class Virtual(
        override val topic: String,
        override val scheduleTime: String,
        override val hostName: String,
        val notifyUser: Boolean = true,
        val googleMeetUrl: String
    ) : Meeting()

    data class Physical(
        override val topic: String,
        override val scheduleTime: String,
        override val hostName: String,
        val venueLocation: String
    ) : Meeting()
}

@Composable
fun MeetingScreen() {
    // State variables for meeting details
    var meetingType by remember { mutableStateOf("Virtual") } // Virtual or Physical
    var topic by remember { mutableStateOf("") }
    var scheduleTime by remember { mutableStateOf("") }
    var hostName by remember { mutableStateOf("") }
    var venueLocation by remember { mutableStateOf("") }
    var notifyUser by remember { mutableStateOf(true) }
    var googleMeetUrl by remember { mutableStateOf("") }
    var urlError by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Center the entire layout vertically and horizontally
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Create Meeting", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            // Meeting type selection using radio buttons
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = meetingType == "Virtual",
                    onClick = { meetingType = "Virtual" }
                )
                Text("Virtual Meeting", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(
                    selected = meetingType == "Physical",
                    onClick = { meetingType = "Physical" }
                )
                Text("Physical Meeting", style = MaterialTheme.typography.bodyLarge)
            }
            Spacer(modifier = Modifier.height(24.dp))

            // Common Field: Topic Name
            OutlinedTextField(
                value = topic,
                onValueChange = { topic = it },
                label = { Text("Topic Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Schedule Time field with a button to launch a date/time picker.
            OutlinedTextField(
                value = scheduleTime,
                onValueChange = { /* Read-only field */ },
                label = { Text("Schedule Time") },
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        // Replace with your own date icon
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_my_calendar),
                            contentDescription = "Select Date"
                        )
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Common Field: Host Name
            OutlinedTextField(
                value = hostName,
                onValueChange = { hostName = it },
                label = { Text("Host Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Display fields specific to Virtual meeting
            if (meetingType == "Virtual") {
                // Notify user Switch (defaulted to On)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Notify Users", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.width(16.dp))
                    Switch(
                        checked = notifyUser,
                        onCheckedChange = { notifyUser = it }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                // Google Meet URL input with basic validation
                OutlinedTextField(
                    value = googleMeetUrl,
                    onValueChange = {
                        googleMeetUrl = it
                        urlError = if (googleMeetUrl.isNotEmpty() && !googleMeetUrl.startsWith("https://meet.google.com/"))
                            "Enter a valid Google Meet URL" else ""
                    },
                    label = { Text("Google Meet URL") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                )
                if (urlError.isNotEmpty()) {
                    Text(urlError, color = MaterialTheme.colorScheme.error)
                }
                Spacer(modifier = Modifier.height(16.dp))
                // Button to launch the Google Meet app using an intent
                Button(
                    onClick = { launchGoogleMeet(context) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Create New Meeting in Google Meet")
                }
            } else {
                // Display field specific to Physical meeting: Venue Location
                OutlinedTextField(
                    value = venueLocation,
                    onValueChange = { venueLocation = it },
                    label = { Text("Venue Location") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            // Save meeting button that prints the meeting data for debug purposes.
            Button(
                onClick = {
                    if (topic.isBlank() || scheduleTime.isBlank() || hostName.isBlank()) {
                        Toast.makeText(context, "Please fill in all required fields", Toast.LENGTH_LONG).show()
                        return@Button
                    }
                    if (meetingType == "Virtual") {
                        if (googleMeetUrl.isNotEmpty() && !googleMeetUrl.startsWith("https://meet.google.com/")) {
                            Toast.makeText(context, "Invalid Google Meet URL", Toast.LENGTH_LONG).show()
                            return@Button
                        }
                        val meeting = Meeting.Virtual(
                            topic = topic,
                            scheduleTime = scheduleTime,
                            hostName = hostName,
                            notifyUser = notifyUser,
                            googleMeetUrl = googleMeetUrl
                        )
                        println("Meeting Data: $meeting")
                    } else {
                        if (venueLocation.isBlank()) {
                            Toast.makeText(context, "Please fill in the venue location", Toast.LENGTH_LONG).show()
                            return@Button
                        }
                        val meeting = Meeting.Physical(
                            topic = topic,
                            scheduleTime = scheduleTime,
                            hostName = hostName,
                            venueLocation = venueLocation
                        )
                        println("Meeting Data: $meeting")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Meeting")
            }
        }
        // Date/Time Picker integration (simulate using an AlertDialog)
        if (showDatePicker) {
            AlertDialog(
                onDismissRequest = { showDatePicker = false },
                title = { Text("Select Schedule Time") },
                text = { Text("Date/Time picker would appear here.") },
                confirmButton = {
                    TextButton(onClick = {
                        // In a real integration use a compose date picker library (like ComposeDatePicker)
                        // For demonstration, we simulate selecting a date and time
                        scheduleTime = "2025-03-25 10:00"
                        showDatePicker = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

// Helper function to launch the Google Meet app
private fun launchGoogleMeet(context: Context) {
    val packageManager = context.packageManager
    // Replace with the actual package name of Google Meet
    val intent: Intent? = packageManager.getLaunchIntentForPackage("com.google.android.apps.meetings")
    if (intent != null) {
        context.startActivity(intent)
    } else {
        Toast.makeText(context, "Google Meet app is not installed", Toast.LENGTH_LONG).show()
    }
}