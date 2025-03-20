package dcrustm.ecell.mobile.domain.dummy

import dcrustm.ecell.mobile.data.local.profile.ProfileEntity

interface ProfileRepository {

    suspend fun fetchAndSaveProfile(email: String)
    suspend fun getLocalProfile(): ProfileEntity?

}