package dcrustm.ecell.mobile.domain.usecase.auth

import dcrustm.ecell.mobile.domain.auth.AuthResult
import dcrustm.ecell.mobile.domain.dummy.AuthRepository
import javax.inject.Inject

class EmailLoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {

    suspend operator fun invoke(email: String, password: String) : AuthResult {
        return authRepository.emailPasswordSignIn(email, password)
    }

}