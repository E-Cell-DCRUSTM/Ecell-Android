package dcrustm.ecell.mobile.domain.dummy

import dcrustm.ecell.mobile.data.local.image.ImageEntity
import kotlinx.coroutines.flow.Flow
import java.io.File

interface ImageRepository {

    fun getImagesFlow(): Flow<List<ImageEntity>>
    suspend fun refreshImages(fetchType: RefreshType): Result<Unit>
    suspend fun uploadImage(file: File, caption: String, authToken: String): Result<ImageEntity>
    suspend fun deleteAllImages(): Result<Unit>

}

enum class RefreshType {
    FULL, // Replace the local cache completely.
    INCREMENTAL // Append any new images (using the last fetch timestamp in metadata).
}