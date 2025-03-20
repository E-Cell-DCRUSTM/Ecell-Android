package dcrustm.ecell.mobile.data.repository

import dcrustm.ecell.mobile.data.local.profile.ProfileDao
import dcrustm.ecell.mobile.data.local.profile.ProfileEntity
import dcrustm.ecell.mobile.data.remote.model.ProfileResponseDTO
import dcrustm.ecell.mobile.data.remote.model.toEntity
import dcrustm.ecell.mobile.domain.dummy.ProfileRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import javax.inject.Inject
import javax.inject.Named

class ProfileRepositoryImpl @Inject constructor(
    private val client: HttpClient,
    @Named("base_url") private val baseUrl: String,
    private val profileDao: ProfileDao
) : ProfileRepository {

    override suspend fun fetchAndSaveProfile(email: String) {

        // Construct the URL with query parameter
        val url = "$baseUrl/api/users/fetch"

        // Make the GET request; Ktor automatically uses the configured JSON converter
        val response: ProfileResponseDTO = client.get(url) {
            parameter("email", email)
        }.body()

        println("Response received: $response")

        // Convert the DTO to a local entity (ignoring id and createdAt from the response)
        val profileEntity = response.toEntity()
        println("Response received: $profileEntity")

        // Store the profile locally – OnConflictStrategy.REPLACE guarantees that only one entry exists.
        profileDao.insertUser(profileEntity)
    }

    override suspend fun getLocalProfile(): ProfileEntity? {
        return profileDao.getUser()
    }

}