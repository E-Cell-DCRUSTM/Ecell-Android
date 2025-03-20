package dcrustm.ecell.mobile.domain.model

data class GoogleProviderUser(
    val oauthGoogle: String? = null,
    val fullName: String? = null,
    val email: String,
    val photoUrl: String? = null
)

fun GoogleProviderUser.toUser(): User {
    val (firstName, lastName) = fullName!!.split(" ", limit = 2).let {
        it.first() to it.getOrNull(1)
    }
    return User(
        firstName = firstName,
        lastName = lastName,
        email = email,
        password = null,
        photoUrl = photoUrl,
        oauthGoogle = oauthGoogle
    )
}
