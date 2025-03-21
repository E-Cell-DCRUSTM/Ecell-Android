package dcrustm.ecell.mobile.ui.admin

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dcrustm.ecell.mobile.domain.dummy.ImageRepository
import dcrustm.ecell.mobile.domain.dummy.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

sealed class GalleryUploadUiState {
    object Idle : GalleryUploadUiState()
    data class ImageSelected(val file: File) : GalleryUploadUiState()
    object Uploading : GalleryUploadUiState()
    object Success : GalleryUploadUiState()
    data class Error(val message: String) : GalleryUploadUiState()
}

// -------------------------
// 2. AdminGalleryUploadViewModel
// -------------------------

@HiltViewModel
class AdminGalleryUploadViewModel @Inject constructor(
    private val imageRepository: ImageRepository, // Repository with uploadImage(file, caption, token)
    private val profileRepository: ProfileRepository  // Used for getAccessToken()
) : ViewModel() {

    private val _uiState = MutableStateFlow<GalleryUploadUiState>(GalleryUploadUiState.Idle)
    val uiState: StateFlow<GalleryUploadUiState> = _uiState.asStateFlow()

    fun setSelectedImage(file: File) {
        _uiState.value = GalleryUploadUiState.ImageSelected(file)
    }

    fun uploadImage(caption: String) {
        val currentState = _uiState.value
        if (currentState is GalleryUploadUiState.ImageSelected) {
            val file = currentState.file
            val effectiveCaption = if (caption.isBlank()) "Caption not provided" else caption
            viewModelScope.launch {
                _uiState.value = GalleryUploadUiState.Uploading
                try {
                    val token = profileRepository.getAccessToken()
                    val result = imageRepository.uploadImage(file, effectiveCaption, token)
                    if (result.isSuccess) {
                        _uiState.value = GalleryUploadUiState.Success
                    } else {
                        _uiState.value = GalleryUploadUiState.Error(result.exceptionOrNull()?.localizedMessage ?: "Upload failed")
                    }
                } catch (e: Exception) {
                    _uiState.value = GalleryUploadUiState.Error(e.localizedMessage ?: "Upload failed")
                }
            }
        }
    }

    fun resetState() {
        _uiState.value = GalleryUploadUiState.Idle
    }
}

fun copyUriToFile(context: Context, uri: Uri): File {
    val inputStream = context.contentResolver.openInputStream(uri)
    val tempFile = File.createTempFile("upload_", ".jpg", context.cacheDir)
    inputStream.use { input ->
        tempFile.outputStream().use { output ->
            input?.copyTo(output)
        }
    }
    return tempFile
}

fun createFileFromBitmap(context: Context, bitmap: Bitmap): File {
    val tempFile = File.createTempFile("camera_", ".jpg", context.cacheDir)
    FileOutputStream(tempFile).use { outputStream ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
    }
    return tempFile
}