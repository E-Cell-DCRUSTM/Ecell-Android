package dcrustm.ecell.mobile.domain.model

import kotlinx.serialization.Serializable

data class User(
    val firstName: String,
    val lastName: String? = null,
    val email: String,
    val password: String?,
    val photoUrl: String? = null,
    val oauthGoogle: String? = null,
    val role: ROLE = ROLE.MEMBER
)

@Serializable
data class UserDto(
    val firstName: String,
    val lastName: String? = null,
    val email: String,
    val password: String?,
    val photoUrl: String? = null,
    val oauthGoogle: String? = null,
    val role: String
)

fun User.toUserDto(): UserDto {
    return UserDto(
        firstName = firstName,
        lastName = lastName,
        email = email,
        password = password,
        photoUrl = photoUrl,
        oauthGoogle = oauthGoogle,
        role = role.name
    )
}