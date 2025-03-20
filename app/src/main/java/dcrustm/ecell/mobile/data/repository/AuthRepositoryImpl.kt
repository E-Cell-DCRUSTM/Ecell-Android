package dcrustm.ecell.mobile.data.repository


import android.content.Context
import dcrustm.ecell.mobile.data.remote.model.AuthResponse
import dcrustm.ecell.mobile.domain.auth.AuthError
import dcrustm.ecell.mobile.domain.auth.AuthResult
import dcrustm.ecell.mobile.domain.auth.AuthSuccess
import dcrustm.ecell.mobile.domain.dummy.AuthRepository
import dcrustm.ecell.mobile.domain.model.GoogleProviderUser
import dcrustm.ecell.mobile.domain.model.User
import dcrustm.ecell.mobile.domain.model.toUserDto
import dcrustm.ecell.mobile.domain.usecase.auth.GetGoogleIdUseCase
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import javax.inject.Inject
import javax.inject.Named

class AuthRepositoryImpl @Inject constructor(
    private val client: HttpClient,
    private val getGoogleIdUseCase: GetGoogleIdUseCase,
    @Named("base_url") private val baseUrl: String
) : AuthRepository {

    override suspend fun googleSignUp(user: User): AuthResult {

        return try {

            val response: AuthResponse = client.post("$baseUrl/api/users") {
                contentType(ContentType.Application.Json)
                setBody(user.toUserDto())
            }.body()


            println("Access Token: ${response.accessToken}")
            println("Refresh Token: ${response.refreshToken}")
            AuthSuccess(response.accessToken, response.refreshToken)

        } catch (e: ClientRequestException) {

            if (e.response.status == HttpStatusCode.Conflict) {
                println("User already exists, consider login instead")
                AuthError("User already exists, consider login instead")
            } else {
                println("Client error: ${e.message}")
                AuthError("Client error: ${e.message}")
            }

        } catch (e: Exception) {

            // Handle network errors and any other exceptions
            println("Network error: ${e.message}")
            AuthError("Network error: ${e.message}")
        }

    }

    override suspend fun emailSignUp(user: User): AuthResult {

        return try {

            val response: AuthResponse = client.post("$baseUrl/api/users") {
                contentType(ContentType.Application.Json)
                setBody(user.toUserDto())
            }.body()


            println("Access Token: ${response.accessToken}")
            println("Refresh Token: ${response.refreshToken}")
            AuthSuccess(response.accessToken, response.refreshToken)

        } catch (e: ClientRequestException) {

            if (e.response.status == HttpStatusCode.Conflict) {
                println("User already exists, consider login instead")
                AuthError("User already exists, consider login instead")
            } else {
                println("Client error: ${e.message}")
                AuthError("Client error: ${e.message}")
            }

        } catch (e: Exception) {

            // Handle network errors and any other exceptions
            println("Network error: ${e.message}")
            AuthError("Network error: ${e.message}")
        }
    }
}