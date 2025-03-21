package dcrustm.ecell.mobile.ui.admin

import android.Manifest
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter

@Composable
fun UploadGalleryDialog(
    viewModel: AdminGalleryUploadViewModel = hiltViewModel(),
    onDismiss: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var captionText by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current

    // Launcher for selecting image from gallery.
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val file = copyUriToFile(context, uri)
            viewModel.setSelectedImage(file)
        } else {
            Toast.makeText(context, "No image selected", Toast.LENGTH_SHORT).show()
        }
    }

    // Launcher for taking photo from camera.
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            val file = createFileFromBitmap(context, bitmap)
            viewModel.setSelectedImage(file)
        } else {
            Toast.makeText(context, "No image captured", Toast.LENGTH_SHORT).show()
        }
    }

    // Request camera permission.
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            cameraLauncher.launch()
        } else {
            Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    Dialog(onDismissRequest = { onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Always show the header text.
                Text(
                    text = "Upload Gallery Image",
                    style = MaterialTheme.typography.titleLarge
                )
                // If we are in an event state (Success or Error), hide input areas.
                when (uiState) {
                    is GalleryUploadUiState.Success -> {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Upload Success",
                            tint = Color.Green,
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = "Upload successful!",
                            style = MaterialTheme.typography.titleLarge
                        )
                        ElevatedButton(
                            onClick = {
                                viewModel.resetState()
                                onDismiss()
                            }
                        ) {
                            Text("OK")
                        }
                    }
                    is GalleryUploadUiState.Error -> {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Upload Error",
                            tint = Color.Red,
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = (uiState as GalleryUploadUiState.Error).message,
                            color = Color.Red,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleLarge
                        )
                        ElevatedButton(
                            onClick = {
                                viewModel.resetState()
                                onDismiss()
                            }
                        ) {
                            Text("OK")
                        }
                    }
                    else -> {
                        // Normal state (Idle, ImageSelected, Uploading) – show full UI.
                        // Row for selecting images.
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(onClick = { galleryLauncher.launch("image/*") }) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = "Select from Gallery"
                                )
                            }
                            IconButton(onClick = {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Take a Photo"
                                )
                            }
                        }
                        // Image preview area.
                        if (uiState is GalleryUploadUiState.ImageSelected) {
                            val file = (uiState as GalleryUploadUiState.ImageSelected).file
                            Image(
                                painter = rememberAsyncImagePainter(file),
                                contentDescription = "Selected Image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(12.dp)), // Clipped into a rounded rectangle.
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            // When no image is selected, show a centered Camera icon and text.
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "No Image",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No image selected",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        // Caption input.
                        OutlinedTextField(
                            value = captionText,
                            onValueChange = { captionText = it },
                            label = { Text("Caption (optional)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        // Show uploading indicator if necessary.
                        if (uiState is GalleryUploadUiState.Uploading) {
                            CircularProgressIndicator()
                        }
                        // Bottom action buttons – Upload and a Close icon.
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ElevatedButton(
                                onClick = { viewModel.uploadImage(captionText) },
                                enabled = uiState is GalleryUploadUiState.ImageSelected && uiState !is GalleryUploadUiState.Uploading
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CloudUpload,
                                    contentDescription = "Upload"
                                )
                            }
                            ElevatedButton(
                                onClick = { onDismiss() }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = "Close"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}