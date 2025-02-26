package dcrustm.ecell.mobile.domain.dummy

import android.app.Activity
import androidx.credentials.CredentialManager
import dcrustm.ecell.mobile.domain.model.User

interface AuthRepository {
    suspend fun signInWithGoogle(credentialManager: CredentialManager, activity: Activity): Result<User>
    suspend fun signOut(): Result<Unit>
    fun getCurrentUser(): User?
    fun isUserAuthenticated(): Boolean
}
