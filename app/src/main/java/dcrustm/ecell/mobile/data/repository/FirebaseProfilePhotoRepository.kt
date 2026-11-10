package dcrustm.ecell.mobile.data.repository

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.provider.OpenableColumns
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import dcrustm.ecell.mobile.data.firebase.awaitResult
import dcrustm.ecell.mobile.domain.repository.ProfilePhotoRepository
import dcrustm.ecell.mobile.domain.repository.UploadedProfilePhoto
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject

class FirebaseProfilePhotoRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val storage: FirebaseStorage
) : ProfilePhotoRepository {

    override suspend fun uploadOwnProfilePhoto(uid: String, uri: Uri): UploadedProfilePhoto =
        withContext(Dispatchers.IO) {
            require(uid.isNotBlank()) { "Sign in again before selecting a profile photo." }
            validateSource(uri)
            val bitmap = decodeBounded(uri)
            val square = centerCropAndResize(bitmap)
            val bytes = compressJpeg(square)
            val path = "memberPhotos/$uid/profile.jpg"
            val metadata = StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .setCacheControl("private, max-age=0, no-transform")
                .build()
            storage.reference.child(path).putBytes(bytes, metadata).awaitResult()
            UploadedProfilePhoto(storagePath = path, preview = square)
        }

    override suspend fun loadProfilePhoto(uid: String, storagePath: String): Bitmap? =
        withContext(Dispatchers.IO) {
            if (uid.isBlank() || storagePath != "memberPhotos/$uid/profile.jpg") return@withContext null
            runCatching {
                val bytes = storage.reference.child(storagePath)
                    .getBytes(MAX_COMPRESSED_BYTES.toLong()).awaitResult()
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            }.getOrNull()
        }

    private fun validateSource(uri: Uri) {
        val mimeType = context.contentResolver.getType(uri)?.lowercase()
        require(mimeType == "image/jpeg" || mimeType == "image/png") {
            "Choose a JPEG or PNG image."
        }
        val size = querySize(context.contentResolver, uri)
        require(size == null || size in 1..MAX_SOURCE_BYTES) {
            "Choose an image smaller than 12 MB."
        }
    }

    private fun querySize(resolver: ContentResolver, uri: Uri): Long? = runCatching {
        resolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { cursor ->
            if (!cursor.moveToFirst()) null else {
                val index = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (index < 0 || cursor.isNull(index)) null else cursor.getLong(index)
            }
        }
    }.getOrNull()

    private fun decodeBounded(uri: Uri): Bitmap {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        return ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            val scale = minOf(1f, MAX_DECODE_DIMENSION.toFloat() /
                maxOf(info.size.width, info.size.height).coerceAtLeast(1))
            decoder.setTargetSize(
                (info.size.width * scale).toInt().coerceAtLeast(1),
                (info.size.height * scale).toInt().coerceAtLeast(1)
            )
        }
    }

    private fun centerCropAndResize(source: Bitmap): Bitmap {
        val side = minOf(source.width, source.height)
        val left = (source.width - side) / 2
        val top = (source.height - side) / 2
        val cropped = Bitmap.createBitmap(source, left, top, side, side)
        val resized = Bitmap.createScaledBitmap(cropped, OUTPUT_DIMENSION, OUTPUT_DIMENSION, true)
        if (cropped !== source && cropped !== resized) cropped.recycle()
        if (source !== resized) source.recycle()
        return resized
    }

    private fun compressJpeg(bitmap: Bitmap): ByteArray {
        listOf(88, 80, 72, 64).forEach { quality ->
            val output = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, output)
            val bytes = output.toByteArray()
            if (bytes.size <= MAX_COMPRESSED_BYTES) return bytes
        }
        error("This photo could not be compressed small enough. Choose another image.")
    }

    private companion object {
        const val MAX_SOURCE_BYTES = 12L * 1024L * 1024L
        const val MAX_COMPRESSED_BYTES = 350 * 1024
        const val MAX_DECODE_DIMENSION = 2048
        const val OUTPUT_DIMENSION = 512
    }
}
