package dcrustm.ecell.mobile.domain.dummy

import dcrustm.ecell.mobile.domain.auth.AuthResult
import dcrustm.ecell.mobile.domain.model.User

interface AuthRepository {

    suspend fun emailSignUp(user: User): AuthResult

    suspend fun googleSignUp(user: User): AuthResult

    suspend fun emailSignIn(user: User): AuthResult

    suspend fun googleSignIn(user: User): AuthResult

}