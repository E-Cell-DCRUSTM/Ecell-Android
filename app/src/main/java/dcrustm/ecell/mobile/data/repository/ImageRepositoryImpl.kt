package dcrustm.ecell.mobile.data.repository

import dcrustm.ecell.mobile.data.local.image.ImageDao
import dcrustm.ecell.mobile.data.local.image.ImageEntity
import dcrustm.ecell.mobile.data.local.image.ImageMetadataDao
import dcrustm.ecell.mobile.data.local.image.ImageMetadataEntity
import dcrustm.ecell.mobile.data.remote.image.ImagesApiService
import dcrustm.ecell.mobile.data.remote.image.toEntity
import dcrustm.ecell.mobile.domain.dummy.ImageRepository
import dcrustm.ecell.mobile.domain.dummy.RefreshType
import kotlinx.coroutines.flow.Flow
import java.io.File
import javax.inject.Inject

class ImageRepositoryImpl @Inject constructor(
    private val imageDao: ImageDao,
    private val metadataDao: ImageMetadataDao,
    private val apiService: ImagesApiService
) : ImageRepository {

    override fun getImagesFlow(): Flow<List<ImageEntity>> {
        return imageDao.getAllImages()
    }

    override suspend fun refreshImages(fetchType: RefreshType): Result<Unit> {
        return try {
            when (fetchType) {
                RefreshType.FULL -> {
                    // Fetch all images from remote endpoint.
                    val images = apiService.fetchAllImages()
                    // Overwrite the local cache.
                    imageDao.deleteAllImages()
                    imageDao.insertImages(images.map { it.toEntity() })
                    // Update metadata with the current time.
                    val nowIso = getCurrentIsoTime()
                    metadataDao.insertMetadata(ImageMetadataEntity(lastFetchDate = nowIso))
                    Result.success(Unit)
                }
                RefreshType.INCREMENTAL -> {
                    // Get the last fetch timestamp from metadata.
                    val metadata = metadataDao.getMetadata()
                    val after = metadata?.lastFetchDate.orEmpty()
                    val newImages = if (after.isEmpty()) {
                        apiService.fetchAllImages()
                    } else {
                        apiService.fetchImagesAfter(after)
                    }
                    if (newImages.isNotEmpty()) {
                        // Insert new images (using REPLACE strategy ensures no duplicates).
                        imageDao.insertImages(newImages.map { it.toEntity() })
                        // Update lastFetchDate with the latest creationTime from the new images.
                        val latestTime =
                            newImages.maxByOrNull { it.creationTime }?.creationTime ?: getCurrentIsoTime()
                        metadataDao.insertMetadata(ImageMetadataEntity(lastFetchDate = latestTime))
                    }
                    Result.success(Unit)
                }
            }
        } catch (e: Exception) {
            // Centralize error handling and propagate the error.
            Result.failure(e)
        }
    }

    override suspend fun uploadImage(
        file: File,
        caption: String,
        authToken: String
    ): Result<ImageEntity> {
        return try {
            val response = apiService.uploadImage(file, caption, authToken)
            val entity = response.toEntity()
            // Optionally insert the newly uploaded image to the local cache.
            imageDao.insertImages(listOf(entity))
            Result.success(entity)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAllImages(): Result<Unit> {
        return try {
            imageDao.deleteAllImages()
            // Reset metadata.
            metadataDao.insertMetadata(ImageMetadataEntity(lastFetchDate = null))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Helper method to get current time in ISO-8601 format.
    private fun getCurrentIsoTime(): String {
        return java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC).toString()
    }

}