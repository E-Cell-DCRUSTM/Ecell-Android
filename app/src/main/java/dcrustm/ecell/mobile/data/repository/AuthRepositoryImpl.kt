package dcrustm.ecell.mobile.data.repository

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dcrustm.ecell.mobile.data.model.toDomainUser
import dcrustm.ecell.mobile.domain.dummy.AuthRepository
import dcrustm.ecell.mobile.domain.model.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth
) : AuthRepository {

    override suspend fun signInWithGoogle(credentialManager: CredentialManager, activity: Activity): Result<User> {
        return try {
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
            val result = credentialManager.getCredential(request = request, context = activity)

            // Extract the Google ID token from the response
            val googleIdTokenCredential =
                GoogleIdTokenCredential.createFrom(result.credential.data)
            val idToken = googleIdTokenCredential.idToken
                ?: throw Exception("No ID Token received from Credential Manager")

            // Exchange the Google ID token for a Firebase credential
            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)

            // Sign in with Firebase Authentication using the credential
            val authResult = auth.signInWithCredential(firebaseCredential).await()

            // Convert the Firebase user to your domain user model
            authResult.user?.let { firebaseUser ->
                Result.success(firebaseUser.toDomainUser())
            } ?: Result.failure(Exception("Firebase user is null"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getCurrentUser(): User? {
        return auth.currentUser?.toDomainUser()
    }

    override fun isUserAuthenticated(): Boolean {
        return auth.currentUser != null
    }

}
