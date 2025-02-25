package dcrustm.ecell.mobile.domain.usecase.auth

import dcrustm.ecell.mobile.domain.dummy.AuthRepository
import javax.inject.Inject

class IsUserAuthenticatedUseCase @Inject constructor(
    private val repository: AuthRepository
) {

    operator fun invoke(): Boolean {
        return repository.isUserAuthenticated()
    }

}