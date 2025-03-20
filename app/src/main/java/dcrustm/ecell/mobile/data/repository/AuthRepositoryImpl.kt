package dcrustm.ecell.mobile.data.repository


import dcrustm.ecell.mobile.data.remote.model.AuthResponse
import dcrustm.ecell.mobile.domain.auth.AuthError
import dcrustm.ecell.mobile.domain.auth.AuthResult
import dcrustm.ecell.mobile.domain.auth.AuthSuccess
import dcrustm.ecell.mobile.domain.dummy.AuthRepository
import dcrustm.ecell.mobile.domain.model.User
import dcrustm.ecell.mobile.domain.model.toUserDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.utils.EmptyContent.contentType
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Named

class AuthRepositoryImpl @Inject constructor(
    private val client: HttpClient,
    @Named("base_url") private val baseUrl: String
) : AuthRepository {

    override suspend fun emailSignUp(user: User): AuthResult {

        println(user)

        return try {
            println(user)
            val response: AuthResponse = client.post("$baseUrl/api/users") {
                contentType(ContentType.Application.Json)
                // Ktor will automatically convert the User object into JSON (thanks to the json serializer)
                setBody(user.toUserDto())
            }.body()
            // For now, we simply print out the tokens on success.
            println("Access Token: ${response.accessToken}")
            println("Refresh Token: ${response.refreshToken}")
            AuthSuccess(response.accessToken, response.refreshToken)

        } catch (e: ClientRequestException) {
            // This exception is thrown for 4xx responses.
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