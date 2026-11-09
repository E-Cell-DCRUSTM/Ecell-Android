package dcrustm.ecell.mobile.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dcrustm.ecell.mobile.session.UserSessionState

@Composable
fun SessionGateContent(
    state: UserSessionState,
    onRetry: () -> Unit,
    onSignOut: () -> Unit,
    approvedContent: @Composable () -> Unit
) {
    when (state) {
        UserSessionState.SignedOut -> SessionStatusScreen(state, onRetry, onSignOut)
        UserSessionState.Loading -> SessionStatusScreen(state, onRetry, onSignOut)
        is UserSessionState.Pending -> PendingApprovalScreen(onSignOut)
        is UserSessionState.Approved -> approvedContent()
        UserSessionState.ProfileUnavailable,
        UserSessionState.NetworkFailure,
        UserSessionState.PermissionDenied,
        UserSessionState.RetryableFailure -> SessionStatusScreen(state, onRetry, onSignOut)
    }
}

@Composable
fun SessionStatusScreen(
    state: UserSessionState,
    onRetry: () -> Unit,
    onSignOut: () -> Unit
) {
    val (title, message, canRetry, canSignOut) = when (state) {
        UserSessionState.Loading -> StatusCopy(
            "Checking your account", "Loading your E-Cell membership profile.", false, false
        )
        UserSessionState.ProfileUnavailable -> StatusCopy(
            "Profile unavailable",
            "Your account is signed in, but its membership profile could not be found. Retry, or sign out and sign in again to restore or reapply for membership.",
            true,
            true
        )
        UserSessionState.NetworkFailure -> StatusCopy(
            "Connection problem",
            "We could not reach your membership profile. Check your connection and retry.",
            true,
            true
        )
        UserSessionState.PermissionDenied -> StatusCopy(
            "Access denied",
            "Firebase denied access to your membership profile. Retry, or sign out and use the account approved for this app.",
            true,
            true
        )
        UserSessionState.RetryableFailure -> StatusCopy(
            "Could not load your profile",
            "Something interrupted the profile check. Retry, or sign out and sign in again.",
            true,
            true
        )
        UserSessionState.SignedOut -> StatusCopy(
            "Signed out", "Sign in to continue to the E-Cell workspace.", false, false
        )
        is UserSessionState.Pending, is UserSessionState.Approved -> StatusCopy(
            "Account ready", "", false, false
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (state == UserSessionState.Loading) {
            CircularProgressIndicator()
            Spacer(Modifier.height(24.dp))
        }
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center)
        if (message.isNotBlank()) {
            Spacer(Modifier.height(12.dp))
            Text(message, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
        if (canRetry) {
            Spacer(Modifier.height(24.dp))
            Button(onClick = onRetry) { Text("Retry") }
        }
        if (canSignOut) {
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onSignOut) { Text("Sign out") }
        }
    }
}

private data class StatusCopy(
    val title: String,
    val message: String,
    val canRetry: Boolean,
    val canSignOut: Boolean
)
