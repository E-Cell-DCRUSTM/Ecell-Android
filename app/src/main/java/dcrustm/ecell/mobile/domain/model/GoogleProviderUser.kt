package dcrustm.ecell.mobile.domain.model

data class GoogleProviderUser(
    val oauthGoogle: String,
    val fullName: String,
    val email: String,
    val photoUrl: String?
)

fun GoogleProviderUser.toUser(): User {
    val (firstName, lastName) = fullName.split(" ", limit = 2).let {
        it.first() to it.getOrNull(1)
    }
    return User(
        firstName = firstName,
        lastName = lastName,
        email = email,
        password = "",
        photoUrl = photoUrl,
        oauthGoogle = oauthGoogle
    )
}
