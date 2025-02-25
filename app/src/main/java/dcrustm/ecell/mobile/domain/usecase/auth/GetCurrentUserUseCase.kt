package dcrustm.ecell.mobile.domain.usecase.auth

import dcrustm.ecell.mobile.domain.dummy.AuthRepository
import dcrustm.ecell.mobile.domain.model.User
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val repository: AuthRepository
) {

    operator fun invoke(): User? {
        return repository.getCurrentUser()
    }

}