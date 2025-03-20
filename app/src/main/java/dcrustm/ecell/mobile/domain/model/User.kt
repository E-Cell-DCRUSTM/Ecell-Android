package dcrustm.ecell.mobile.domain.model

data class User(
    val firstName: String,
    val lastName: String? = null,
    val email: String,
    val password: String,
    val photoUrl: String? = null,
    val oauthGoogle: String? = null,
    val role: ROLE = ROLE.MEMBER
)
