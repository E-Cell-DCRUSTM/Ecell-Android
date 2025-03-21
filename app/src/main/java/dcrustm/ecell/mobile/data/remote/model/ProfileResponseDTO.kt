package dcrustm.ecell.mobile.data.remote.model

import dcrustm.ecell.mobile.data.local.profile.ProfileEntity
import kotlinx.serialization.Serializable

@Serializable
data class ProfileResponseDTO(

    val id: Int,               // From API, but will be ignored locally
    val firstName: String,
    val lastName: String,
    val email: String,
    val photoUrl: String?,
    val role: String,
    val createdAt: String      // Not needed on local profile

)

fun ProfileResponseDTO.toEntity(): ProfileEntity {
    return ProfileEntity(
        id = 0, // Always constant so only one row exists
        firstName = firstName,
        lastName = lastName,
        email = email,
        photoUrl = photoUrl,
        role = role,
        accessToken = "",
        refreshToken = ""
    )
}