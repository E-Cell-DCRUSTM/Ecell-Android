package dcrustm.ecell.mobile.domain.auth

interface FirebaseAuthRepository {

    suspend fun signInWithGoogle(idToken: String): AuthResult

    suspend fun signInWithEmail(email: String, pass: String): AuthResult

    suspend fun signInAsTestUser(): AuthResult

    fun signOut()
}
