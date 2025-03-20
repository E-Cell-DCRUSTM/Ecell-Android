package dcrustm.ecell.mobile.domain.auth

sealed class AuthResult

data class AuthSuccess(
    val accessToken: String,
    val refreshToken: String
) : AuthResult()


data class AuthError(
    val errorMessage: String
) : AuthResult()