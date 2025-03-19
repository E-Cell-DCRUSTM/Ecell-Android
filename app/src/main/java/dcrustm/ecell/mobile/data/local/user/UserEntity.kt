package dcrustm.ecell.mobile.data.local.user

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_table")
data class UserEntity(
    @PrimaryKey val id: Int,
    val firstName: String,
    val lastName: String,
    val email: String,
    val photoUrl: String,
    val role: String,
    val createdAt: String
)