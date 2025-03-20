package dcrustm.ecell.mobile.domain.model

data class EmailProviderUser(
    val fullName: String,
    val email: String,
    val password: String
)

fun EmailProviderUser.toUser(): User {
    val (firstName, lastName) = fullName.split(" ", limit = 2).let {
        it.first() to it.getOrNull(1)
    }
    return User(
        firstName = firstName,
        lastName = lastName,
        email = email,
        password = password
    )
}

