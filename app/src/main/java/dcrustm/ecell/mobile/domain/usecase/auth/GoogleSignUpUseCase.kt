package dcrustm.ecell.mobile.domain.usecase.auth

import android.app.Activity
import androidx.credentials.CredentialManager
import dcrustm.ecell.mobile.domain.auth.AuthResult
import dcrustm.ecell.mobile.domain.dummy.AuthRepository
import dcrustm.ecell.mobile.domain.model.User
import dcrustm.ecell.mobile.domain.model.toUser
import javax.inject.Inject

class GoogleSignUpUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val getGoogleIdUseCase: GetGoogleIdUseCase
) {

    suspend operator fun invoke(credentialManager: CredentialManager, context: Activity): AuthResult {
        val userData = getUserData(credentialManager, context)
        return authRepository.googleSignUp(userData)
    }

    private suspend fun getUserData(credentialManager: CredentialManager, context: Activity): User {
        val googleProviderUser = getGoogleIdUseCase(credentialManager, context)
        return googleProviderUser.toUser()
    }


}