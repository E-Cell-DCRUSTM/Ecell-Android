package dcrustm.ecell.mobile.ui.profile

import android.net.Uri

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dcrustm.ecell.mobile.ui.directory.RolePill
import dcrustm.ecell.mobile.ui.home.InitialsAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onDismiss: () -> Unit,
    onOpenMemberId: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val member by viewModel.currentUser.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val photoPreview by viewModel.photoPreview.collectAsState()
    val isUploadingPhoto by viewModel.isUploadingPhoto.collectAsState()

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var initialized by remember { mutableStateOf(false) }

    LaunchedEffect(member?.uid) {
        if (!initialized && member != null) {
            name = member?.name ?: ""
            phone = member?.phone ?: ""
            bio = member?.bio ?: ""
            initialized = true
        }
    }

    val hasChanges = member != null && (name != member?.name || phone != member?.phone || bio != member?.bio)
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let(viewModel::selectPhoto)
    }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { captured ->
        val uri = cameraUri
        cameraUri = null
        if (captured && uri != null) viewModel.selectPhoto(uri)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(16.dp)
    ) {
        IconButton(
            onClick = onDismiss,
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Close Profile")
        }

        val m = member
        if (m == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 56.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                errorMessage?.let { message ->
                    Text(message, color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                }
                if (photoPreview != null) {
                    Image(
                        bitmap = photoPreview!!.asImageBitmap(),
                        contentDescription = "Your profile photo",
                        modifier = Modifier.size(88.dp).clip(CircleShape)
                    )
                } else InitialsAvatar(initials = m.initials, size = 88)
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    enabled = !isUploadingPhoto,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Outlined.AddAPhoto, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (isUploadingPhoto) "Uploading photo…" else "Change photo")
                }
                OutlinedButton(
                    onClick = {
                        runCatching { createProfileCaptureUri(context) }
                            .onSuccess { uri -> cameraUri = uri; camera.launch(uri) }
                    },
                    enabled = !isUploadingPhoto,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Take photo")
                }
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(m.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(4.dp))
                RolePill(m.role)
                if (m.isApproved) {
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = onOpenMemberId,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Outlined.Badge, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("  View E-Cell Member ID")
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "${m.committee.displayName} · ${m.yearLabel}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(24.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(100) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Full name") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Email", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(2.dp))
                        Text(m.email.ifBlank { "—" }, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(14.dp))
                        Text("Branch", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(2.dp))
                        Text(m.branch.ifBlank { "—" }, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { if (it.length <= 32) phone = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Phone") },
                    placeholder = { Text("Visible to your committee head and coordinator") },
                    singleLine = true,
                    supportingText = { Text("${phone.length}/32") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = bio,
                    onValueChange = { if (it.length <= 1000) bio = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp),
                    label = { Text("Bio") },
                    supportingText = { Text("${bio.length}/1000") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.updateProfile(name, phone, bio) },
                    enabled = hasChanges && !isSaving,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isSaving) "Saving…" else "Save changes")
                }

                Spacer(Modifier.height(32.dp))

                OutlinedButton(
                    onClick = { viewModel.signOut(); onDismiss() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Sign out")
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
