package dcrustm.ecell.mobile.data.remote.image

import dcrustm.ecell.mobile.data.local.image.ImageEntity
import kotlinx.serialization.Serializable

@Serializable
data class ImageResponse(

    val id: Int,
    val fileName: String,
    val caption: String,
    val creationTime: String,
    val imageUrl: String

)

// Extension function to map remote model to local entity.
fun ImageResponse.toEntity(): ImageEntity {
    return ImageEntity(
        id = this.id,
        fileName = this.fileName,
        caption = this.caption,
        creationTime = this.creationTime,
        imageUrl = this.imageUrl
    )
}
