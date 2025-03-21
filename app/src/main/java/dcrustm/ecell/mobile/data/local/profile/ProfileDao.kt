package dcrustm.ecell.mobile.data.local.profile

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ProfileDao {

    @Query("SELECT * FROM profile_table LIMIT 1")
    suspend fun getUser(): ProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: ProfileEntity)

    @Query("DELETE FROM profile_table")
    suspend fun deleteAllUsers()

}