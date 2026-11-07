package dcrustm.ecell.mobile.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import dcrustm.ecell.mobile.data.firebase.awaitResult
import dcrustm.ecell.mobile.data.firebase.toFirestoreMap
import dcrustm.ecell.mobile.domain.auth.AuthError
import dcrustm.ecell.mobile.domain.auth.AuthResult
import dcrustm.ecell.mobile.domain.auth.AuthSuccess
import dcrustm.ecell.mobile.domain.auth.FirebaseAuthRepository
import dcrustm.ecell.mobile.domain.model.Committee
import dcrustm.ecell.mobile.domain.model.Member
import dcrustm.ecell.mobile.domain.model.ROLE
import javax.inject.Inject

class FirebaseAuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : FirebaseAuthRepository {

    override suspend fun signInWithGoogle(idToken: String): AuthResult {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).awaitResult()
            val firebaseUser = result.user
                ?: return AuthError("Sign-in succeeded but no user was returned.")

            ensureUserDocument(
                uid = firebaseUser.uid,
                displayName = firebaseUser.displayName ?: "",
                email = firebaseUser.email ?: "",
                photoUrl = firebaseUser.photoUrl?.toString() ?: ""
            )

            AuthSuccess(accessToken = firebaseUser.uid, refreshToken = "")
        } catch (e: Exception) {
            AuthError(e.message ?: "Google sign-in failed")
        }
    }

    override fun signOut() {
        firebaseAuth.signOut()
    }

    private suspend fun ensureUserDocument(
        uid: String,
        displayName: String,
        email: String,
        photoUrl: String
    ) {
        val docRef = firestore.collection("users").document(uid)
        val contactRef = firestore.collection("userContacts").document(uid)
        val snapshot = docRef.get().awaitResult()

        if (!snapshot.exists()) {
            val newMember = Member(
                uid = uid,
                name = displayName,
                email = email,
                phone = "",
                year = 1,
                branch = "",
                committee = Committee.NONE,
                role = ROLE.PENDING,
                bio = "",
                profilePhotoUrl = photoUrl,
                joinedAt = System.currentTimeMillis(),
                isApproved = false
            )
            docRef.set(newMember.toFirestoreMap()).awaitResult()
            contactRef.set(
                mapOf("email" to email, "phone" to "", "committee" to Committee.NONE.name)
            ).awaitResult()
        } else {
            val privateEmail = snapshot.getString("email") ?: email
            val privatePhone = snapshot.getString("phone") ?: ""
            val committee = snapshot.getString("committee") ?: Committee.NONE.name
            contactRef.set(
                mapOf("email" to privateEmail, "phone" to privatePhone, "committee" to committee),
                SetOptions.merge()
            ).awaitResult()
            if (snapshot.contains("email") || snapshot.contains("phone")) {
                docRef.update(
                    mapOf("email" to FieldValue.delete(), "phone" to FieldValue.delete())
                ).awaitResult()
            }
            // Refresh profile fields from Google without touching role/isApproved/committee.
            docRef.set(
                mapOf(
                    "name" to displayName,
                    "profilePhotoUrl" to photoUrl
                ),
                SetOptions.merge()
            ).awaitResult()
        }
    }
}
