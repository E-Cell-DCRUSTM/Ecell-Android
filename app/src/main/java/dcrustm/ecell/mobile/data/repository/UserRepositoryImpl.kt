package dcrustm.ecell.mobile.data.repository

import dcrustm.ecell.mobile.data.local.profile.UserDao
import dcrustm.ecell.mobile.data.local.profile.UserEntity
import dcrustm.ecell.mobile.domain.dummy.UserRepository
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao
) : UserRepository {

    override suspend fun getUser(): UserEntity? = userDao.getUser()

    override suspend fun insertUser(user: UserEntity) = userDao.insertUser(user)

    override suspend fun deleteUser() = userDao.deleteAllUsers()

}