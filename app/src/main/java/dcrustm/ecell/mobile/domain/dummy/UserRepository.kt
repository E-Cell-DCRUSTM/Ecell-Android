package dcrustm.ecell.mobile.domain.dummy

import dcrustm.ecell.mobile.data.local.profile.UserEntity

interface UserRepository {

    suspend fun getUser(): UserEntity?
    suspend fun insertUser(user: UserEntity)
    suspend fun deleteUser()

}