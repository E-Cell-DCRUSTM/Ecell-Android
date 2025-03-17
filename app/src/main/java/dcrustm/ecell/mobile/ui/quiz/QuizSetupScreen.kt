package dcrustm.ecell.mobile.ui.quiz

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun QuizSetupScreen(
    viewModel: QuizViewModel = hiltViewModel(),
    onNext: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Create Quiz", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = viewModel.topic,
            onValueChange = { viewModel.topic = it },
            label = { Text("Quiz Topic") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = viewModel.scheduleTime,
            onValueChange = { viewModel.scheduleTime = it },
            label = { Text("Schedule Time (e.g., 2025-03-20 10:00)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = viewModel.durationSeconds,
            onValueChange = { viewModel.durationSeconds = it },
            label = { Text("Duration (seconds: 30 to 120)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        if (viewModel.durationError.isNotEmpty()) {
            Text(text = viewModel.durationError, color = Color.Red)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                val durationInt = viewModel.durationSeconds.toIntOrNull() ?: 0
                if (durationInt < 30 || durationInt > 120) {
                    viewModel.durationError = "Duration must be between 30 and 120 seconds"
                } else {
                    viewModel.durationError = ""
                    onNext()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Next: Add Questions")
        }
    }
}
