package dcrustm.ecell.mobile.data.local.image

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "image_metadata")
data class ImageMetadataEntity(

    // We use a fixed primary key (only one row will be stored)
    @PrimaryKey val id: Int = 0,
    // Stores the last successful full/incremental fetch time (ISO-8601 string)
    val lastFetchDate: String?

)