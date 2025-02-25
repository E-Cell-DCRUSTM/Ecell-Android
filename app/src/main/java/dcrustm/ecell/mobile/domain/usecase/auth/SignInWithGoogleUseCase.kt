package dcrustm.ecell.mobile.domain.usecase.auth

import dcrustm.ecell.mobile.domain.dummy.AuthRepository
import dcrustm.ecell.mobile.domain.model.User
import javax.inject.Inject

class SignInWithGoogleUseCase @Inject constructor(
    private val repository: AuthRepository
) {

    suspend operator fun invoke(idToken: String): Result<User> {
        return repository.signInWithGoogle(idToken)
    }

}