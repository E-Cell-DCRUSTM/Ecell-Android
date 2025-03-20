package dcrustm.ecell.mobile.domain.dummy

import dcrustm.ecell.mobile.domain.auth.AuthResult
import dcrustm.ecell.mobile.domain.model.User

interface AuthRepository {
    suspend fun emailSignUp(user: User): AuthResult
}