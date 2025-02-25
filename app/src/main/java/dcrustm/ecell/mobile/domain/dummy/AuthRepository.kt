package dcrustm.ecell.mobile.domain.dummy

import dcrustm.ecell.mobile.domain.model.User

interface AuthRepository {
    suspend fun signInWithGoogle(idToken: String): Result<User>
    suspend fun signOut(): Result<Unit>
    fun getCurrentUser(): User?
    fun isUserAuthenticated(): Boolean
}
