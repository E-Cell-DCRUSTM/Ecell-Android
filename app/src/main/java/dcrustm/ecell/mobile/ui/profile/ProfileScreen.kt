package dcrustm.ecell.mobile.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import dcrustm.ecell.mobile.R
import dcrustm.ecell.mobile.data.local.profile.ProfileEntity

@Composable
fun ProfileScreen(
    onDismiss: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding() // Takes care of status and navigation bars
            .padding(16.dp)
    ) {
        // Close button at the top-left corner
        IconButton(
            onClick = onDismiss,
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close Profile"
            )
        }

        if (uiState.isLoading) {
            // Show a loading indicator while fetching the profile
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            uiState.profile?.let { profile ->
                // Center profile details on the screen
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Load the profile picture using Coil; if the URL is null or blank, show the placeholder.
                    AsyncImage(
                        model = if (!profile.photoUrl.isNullOrEmpty()) profile.photoUrl else null,
                        contentDescription = "Profile Picture",
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = R.drawable.profile),
                        error = painterResource(id = R.drawable.profile),
                        modifier = Modifier.size(120.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    // Display the full name using MaterialTheme typography
                    Text(
                        text = "${profile.firstName} ${profile.lastName}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // Role text
                    Text(
                        text = profile.role,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // Email text
                    Text(
                        text = profile.email,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } ?: run {
                // Handle the case where no profile is available
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Profile not available", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

// -------------------