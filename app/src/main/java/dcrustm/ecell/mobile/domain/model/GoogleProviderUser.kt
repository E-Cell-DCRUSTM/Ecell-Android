package dcrustm.ecell.mobile.domain.model

data class GoogleProviderUser(
    val idToken: String,
    val oauthGoogle: String? = null,
    val fullName: String? = null,
    val email: String,
    val photoUrl: String? = null
)
