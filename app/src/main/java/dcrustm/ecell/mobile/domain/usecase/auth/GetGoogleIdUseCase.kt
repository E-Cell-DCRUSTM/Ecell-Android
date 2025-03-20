package dcrustm.ecell.mobile.domain.usecase.auth

import android.app.Activity
import android.util.Base64
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dcrustm.ecell.mobile.domain.model.GoogleProviderUser
import org.json.JSONObject
import javax.inject.Inject

class GetGoogleIdUseCase @Inject constructor() {

    suspend operator fun invoke(
        credentialManager: CredentialManager,
        context: Activity
    ) : GoogleProviderUser {

        // Build the Google ID token option
        val googleIdOption = GetGoogleIdOption.Builder()
            .setAutoSelectEnabled(false)
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId("532056230783-tqdvcsfp3f5liro17mriddd1k94jiqjq.apps.googleusercontent.com")
            .build()

        // Create the credential request
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        // Display the Google sign-in dialog and get a credential response
        val result = credentialManager.getCredential(request = request, context = context)

        // Extract the Google ID token from the response
        val tokenCredential =
            GoogleIdTokenCredential.createFrom(result.credential.data)

        val googleProviderUser = GoogleProviderUser(
            oauthGoogle = extractGoogleUserId(tokenCredential.idToken),
            fullName = tokenCredential.displayName,
            photoUrl = tokenCredential.profilePictureUri.toString(),
            email = tokenCredential.id
        )

        println(googleProviderUser)
        return googleProviderUser

    }

    private fun extractGoogleUserId(idToken: String): String? {
        // Split the JWT into header, payload, and signature
        val parts = idToken.split(".")
        if (parts.size != 3) return null

        return try {
            // The payload is the second part of the JWT
            val payload = parts[1]
            // Decode the payload using Base64 URL-safe decoding
            val decodedBytes = Base64.decode(payload, Base64.URL_SAFE or Base64.NO_WRAP)
            val decodedPayload = String(decodedBytes, Charsets.UTF_8)
            // Convert the decoded payload to a JSON object
            val json = JSONObject(decodedPayload)
            // Return the "sub" claim which holds the unique Google user id
            json.getString("sub")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}