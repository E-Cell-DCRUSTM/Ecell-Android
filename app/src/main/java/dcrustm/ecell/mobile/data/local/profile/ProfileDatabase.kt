package dcrustm.ecell.mobile.data.local.profile

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ProfileEntity::class], version = 2, exportSchema = false)
abstract class ProfileDatabase : RoomDatabase() {

    abstract fun profileDao(): ProfileDao

}