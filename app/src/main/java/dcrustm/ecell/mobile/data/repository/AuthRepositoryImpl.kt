package dcrustm.ecell.mobile.data.repository


import dcrustm.ecell.mobile.data.remote.model.AuthResponse
import dcrustm.ecell.mobile.domain.auth.AuthError
import dcrustm.ecell.mobile.domain.auth.AuthResult
import dcrustm.ecell.mobile.domain.auth.AuthSuccess
import dcrustm.ecell.mobile.domain.dummy.AuthRepository
import dcrustm.ecell.mobile.domain.dummy.ProfileRepository
import dcrustm.ecell.mobile.domain.model.User
import dcrustm.ecell.mobile.domain.model.toUserDto
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
    private val profileRepository: ProfileRepository,
    @Named("base_url") private val baseUrl: String
) : AuthRepository {

    override suspend fun googleSignUp(user: User): AuthResult {

        return try {

            val response: AuthResponse = client.post("$baseUrl/api/users") {
                contentType(ContentType.Application.Json)
                setBody(user.toUserDto())
            }.body()

            // Fetch the profile data and store it locally.
            profileRepository.fetchAndSaveProfile(user.email)
            profileRepository.updateToken(response.accessToken, response.refreshToken)
            println("Profile data fetched and stored locally!")

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

            // Fetch the profile data and store it locally.
            profileRepository.fetchAndSaveProfile(user.email)
            profileRepository.updateToken(response.accessToken, response.refreshToken)
            println("Profile data fetched and stored locally!")

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

    override suspend fun googleSignIn(user: User): AuthResult {

        return try {

            // Make a POST request to /api/users/login with a JSON body containing only the email.
            val response: AuthResponse = client.post("$baseUrl/api/users/login") {

                url {
                    parameters.append("provider", "google")
                }

                contentType(ContentType.Application.Json)
                setBody(
                    mapOf(
                        "email" to user.email,
                        "oauthGoogle" to user.oauthGoogle
                    )
                )
            }.body()

            // Fetch the profile data and store it locally.
            profileRepository.fetchAndSaveProfile(user.email)
            profileRepository.updateToken(response.accessToken, response.refreshToken)
            println("Profile data fetched and stored locally!")

            // If successful, print the received response and return AuthSuccess.
            println("Google SignIn Successful Response: $response")
            AuthSuccess(response.accessToken, response.refreshToken)

        } catch (e: ClientRequestException) {

            // Handle client errors: if the response status is HTTP 204, user was not found.
            when (e.response.status) {
                HttpStatusCode.Unauthorized -> {
                    println("Google oauth id is not matching with the database")
                    AuthError("Password doesn't match for the given google id")
                }

                HttpStatusCode.BadRequest -> {
                    println("Bad request to the server")
                    AuthError("Bad request, looks like 'provider' query is missing.")
                }

                HttpStatusCode.NotFound -> {
                    println("User not found")
                    AuthError("User not found")
                }

                else -> {
                    println("Client error: ${e.message}")
                    AuthError("Client error: ${e.message}")
                }
            }



        } catch (e: Exception) {

            // Handle network errors or any other exceptions.
            println("Network error: ${e.message}")
            AuthError("Network error: ${e.message}")
        }
    }

    override suspend fun emailPasswordSignIn(email: String, password: String): AuthResult {
        return try {

            // POST to /api/users/login with both email and password in the request body.
            val response: AuthResponse = client.post("$baseUrl/api/users/login") {

                url {
                    parameters.append("provider", "customemail")
                }

                contentType(ContentType.Application.Json)
                setBody(
                    mapOf(
                        "email" to email,
                        "password" to password
                    )
                )
            }.body()

            // Fetch the profile data and store it locally.
            profileRepository.fetchAndSaveProfile(email)
            profileRepository.updateToken(response.accessToken, response.refreshToken)
            println("Profile data fetched and stored locally!")

            // Assuming a successful response returns the tokens.
            println("Email/Password SignIn Successful Response: $response")

            AuthSuccess(response.accessToken, response.refreshToken)

        } catch (e: ClientRequestException) {

            // Handle specific status codes from your backend.
            when (e.response.status) {
                HttpStatusCode.Unauthorized -> {
                    println("Password doesn't match for the given email id")
                    AuthError("Password doesn't match for the given email id")
                }

                HttpStatusCode.BadRequest -> {
                    println("Bad request to the server")
                    AuthError("Bad request, looks like 'provider' query is missing.")
                }

                HttpStatusCode.NotFound -> {
                    println("User not found")
                    AuthError("User not found")
                }

                else -> {
                    println("Client error: ${e.message}")
                    AuthError("Client error: ${e.message}")
                }
            }
        } catch (e: Exception) {
            println("Network error: ${e.message}")
            AuthError("Network error: ${e.message}")
        }
    }

}