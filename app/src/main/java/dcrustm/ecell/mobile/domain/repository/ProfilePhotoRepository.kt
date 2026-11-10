package dcrustm.ecell.mobile.domain.repository

import android.graphics.Bitmap
import android.net.Uri

data class UploadedProfilePhoto(
    val storagePath: String,
    val preview: Bitmap
)

interface ProfilePhotoRepository {
    suspend fun uploadOwnProfilePhoto(uid: String, uri: Uri): UploadedProfilePhoto
    suspend fun loadProfilePhoto(uid: String, storagePath: String): Bitmap?
}
