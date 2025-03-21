package dcrustm.ecell.mobile.data.local.image

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ImageMetadataDao {

    @Query("SELECT * FROM image_metadata WHERE id = 0")
    suspend fun getMetadata(): ImageMetadataEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetadata(metadata: ImageMetadataEntity)

}