package dcrustm.ecell.mobile.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
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

    override suspend fun signInWithEmail(email: String, pass: String): AuthResult {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, pass).awaitResult()
            val firebaseUser = result.user
                ?: return AuthError("Sign-in succeeded but no user was returned.")

            ensureUserDocument(
                uid = firebaseUser.uid,
                displayName = firebaseUser.displayName ?: email.substringBefore("@"),
                email = firebaseUser.email ?: email,
                photoUrl = firebaseUser.photoUrl?.toString() ?: ""
            )

            AuthSuccess(accessToken = firebaseUser.uid, refreshToken = "")
        } catch (e: Exception) {
            AuthError(e.message ?: "Email sign-in failed")
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
            try {
                docRef.set(newMember.toFirestoreMap()).awaitResult()
                ensureContactDocument(uid, email, "", Committee.NONE.name)
                return
            } catch (createError: Exception) {
                // Two devices may register the same newly authenticated user
                // at once. Reuse the winning profile rather than failing the
                // other sign-in attempt.
                val createdByAnotherDevice = docRef.get().awaitResult()
                if (!createdByAnotherDevice.exists()) throw createError
                ensureContactDocument(
                    uid = uid,
                    fallbackEmail = email,
                    legacyPhone = createdByAnotherDevice.getString("phone") ?: "",
                    committee = createdByAnotherDevice.getString("committee") ?: Committee.NONE.name,
                    legacyEmail = createdByAnotherDevice.getString("email")
                )
                return
            }
        }

        // Existing contact records are deliberately left untouched here: a
        // sign-in must never reset a member's phone number or assigned
        // committee. Legacy fields stay readable to their owner until the
        // explicit Admin SDK migration moves and removes them.
        ensureContactDocument(
            uid = uid,
            fallbackEmail = email,
            legacyPhone = snapshot.getString("phone") ?: "",
            committee = snapshot.getString("committee") ?: Committee.NONE.name,
            legacyEmail = snapshot.getString("email")
        )
    }

    private suspend fun ensureContactDocument(
        uid: String,
        fallbackEmail: String,
        legacyPhone: String,
        committee: String,
        legacyEmail: String? = null
    ) {
        val contactRef = firestore.collection("userContacts").document(uid)
        if (contactRef.get().awaitResult().exists()) return

        try {
            contactRef.set(
                mapOf(
                    "email" to (legacyEmail ?: fallbackEmail),
                    "phone" to legacyPhone,
                    "committee" to committee
                )
            ).awaitResult()
        } catch (createError: Exception) {
            // A concurrent sign-in may have created the contact document.
            if (!contactRef.get().awaitResult().exists()) throw createError
        }
    }
}
