package dcrustm.ecell.mobile.data.local.profile

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_table")
data class ProfileEntity(

    @PrimaryKey val id: Int = 0,
    val firstName: String,
    val lastName: String,
    val email: String,
    val photoUrl: String?,
    val role: String

)