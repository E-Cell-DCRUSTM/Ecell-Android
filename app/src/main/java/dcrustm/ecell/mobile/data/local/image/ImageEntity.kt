package dcrustm.ecell.mobile.data.local.image

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "images")
data class ImageEntity(

    @PrimaryKey val id: Int,
    val fileName: String,
    val caption: String,

    // We store creationTime as a String in ISO-8601 format (or use a converter for Date/LocalDateTime)
    val creationTime: String,
    val imageUrl: String

)