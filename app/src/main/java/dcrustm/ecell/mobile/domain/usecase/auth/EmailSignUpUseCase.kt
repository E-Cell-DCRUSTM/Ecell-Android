package dcrustm.ecell.mobile.domain.usecase.auth

import dcrustm.ecell.mobile.domain.auth.AuthResult
import dcrustm.ecell.mobile.domain.dummy.AuthRepository
import dcrustm.ecell.mobile.domain.model.User
import javax.inject.Inject

class EmailSignUpUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {

    suspend operator fun invoke(user: User): AuthResult {
        return authRepository.emailSignUp(user)
    }
    
}