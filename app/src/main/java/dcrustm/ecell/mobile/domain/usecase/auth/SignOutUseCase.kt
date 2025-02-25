package dcrustm.ecell.mobile.domain.usecase.auth

import dcrustm.ecell.mobile.domain.dummy.AuthRepository
import javax.inject.Inject

class SignOutUseCase @Inject constructor(
    private val repository: AuthRepository
) {

    suspend operator fun invoke(): Result<Unit> {
        return repository.signOut()
    }

}