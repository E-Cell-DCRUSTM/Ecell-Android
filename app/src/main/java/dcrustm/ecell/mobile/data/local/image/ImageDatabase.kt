package dcrustm.ecell.mobile.data.local.image

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ImageEntity::class, ImageMetadataEntity::class], version = 1, exportSchema = false)
abstract class ImageDatabase : RoomDatabase() {

    abstract fun imageDao(): ImageDao
    abstract fun metadataDao(): ImageMetadataDao

}