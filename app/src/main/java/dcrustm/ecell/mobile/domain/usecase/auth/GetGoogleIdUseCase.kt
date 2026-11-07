package dcrustm.ecell.mobile.domain.usecase.auth

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dcrustm.ecell.mobile.domain.model.GoogleProviderUser
import dcrustm.ecell.mobile.R
import javax.inject.Inject

class GetGoogleIdUseCase @Inject constructor() {

    suspend operator fun invoke(
        credentialManager: CredentialManager,
        context: Activity
    ) : GoogleProviderUser {

        // Build the Google ID token option.
        // This must be the Firebase project's WEB (server) OAuth client id —
        // found in google-services.json under oauth_client with client_type: 3.
        val googleIdOption = GetGoogleIdOption.Builder()
            .setAutoSelectEnabled(false)
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(context.getString(R.string.default_web_client_id))
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
            idToken = tokenCredential.idToken,
            fullName = tokenCredential.displayName,
            photoUrl = tokenCredential.profilePictureUri.toString(),
            email = tokenCredential.id
        )

        return googleProviderUser

    }
}
