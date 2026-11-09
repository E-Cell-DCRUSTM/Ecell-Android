package dcrustm.ecell.mobile.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dcrustm.ecell.mobile.data.firebase.awaitResult
import dcrustm.ecell.mobile.data.firebase.snapshots
import dcrustm.ecell.mobile.data.firebase.toMember
import dcrustm.ecell.mobile.domain.model.Committee
import dcrustm.ecell.mobile.domain.model.Member
import dcrustm.ecell.mobile.domain.model.MemberContact
import dcrustm.ecell.mobile.domain.model.ROLE
import dcrustm.ecell.mobile.domain.repository.MemberRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FirestoreMemberRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : MemberRepository {

    private val usersRef get() = firestore.collection("users")

    override fun observeApprovedMembers(): Flow<List<Member>> =
        usersRef.whereEqualTo("isApproved", true).snapshots().map { snap ->
            snap.documents.mapNotNull { it.toMember() }
        }

    override fun observePendingMembers(): Flow<List<Member>> =
        combine(
            usersRef.whereEqualTo("isApproved", false).snapshots(),
            firestore.collection("userContacts").snapshots()
        ) { users, contacts ->
            val contactsByUid = contacts.documents.associate { document ->
                document.id to MemberContact(
                    email = document.getString("email") ?: "",
                    phone = document.getString("phone") ?: ""
                )
            }
            users.documents.mapNotNull { it.toMember()?.let { member ->
                val contact = contactsByUid[member.uid]
                member.copy(email = contact?.email.orEmpty(), phone = contact?.phone.orEmpty())
            } }
        }

    override fun observeMember(uid: String): Flow<Member?> =
        usersRef.document(uid).snapshots().map { it.toMember() }

    override fun observeContact(uid: String): Flow<MemberContact?> =
        firestore.collection("userContacts").document(uid).snapshots().map { snapshot ->
            if (!snapshot.exists()) null else MemberContact(
                email = snapshot.getString("email") ?: "",
                phone = snapshot.getString("phone") ?: ""
            )
        }

    override suspend fun approveMember(uid: String, committee: Committee, role: ROLE) {
        require(committee != Committee.NONE) { "Choose a committee before approving this member." }
        require(role == ROLE.MEMBER || role == ROLE.COMMITTEE_HEAD) {
            "A coordinator role must be granted outside the app."
        }
        val batch = firestore.batch()
        batch.update(usersRef.document(uid), mapOf(
                "isApproved" to true,
                "committee" to committee.name,
                "role" to role.name
            ))
        batch.set(
            firestore.collection("userContacts").document(uid),
            mapOf("committee" to committee.name),
            SetOptions.merge()
        )
        batch.commit().awaitResult()
    }

    override suspend fun assignMember(uid: String, committee: Committee, role: ROLE) {
        require(committee != Committee.NONE) { "Choose a committee for this member." }
        require(role == ROLE.MEMBER || role == ROLE.COMMITTEE_HEAD) {
            "Coordinator roles cannot be assigned from this screen."
        }
        val batch = firestore.batch()
        batch.update(usersRef.document(uid), mapOf(
            "isApproved" to true,
            "committee" to committee.name,
            "role" to role.name
        ))
        batch.set(
            firestore.collection("userContacts").document(uid),
            mapOf("committee" to committee.name),
            SetOptions.merge()
        )
        batch.commit().awaitResult()
    }

    override suspend fun rejectMember(uid: String) {
        val batch = firestore.batch()
        batch.delete(usersRef.document(uid))
        batch.delete(firestore.collection("userContacts").document(uid))
        batch.commit().awaitResult()
    }

    override suspend fun updateOwnProfile(uid: String, phone: String, bio: String) {
        val batch = firestore.batch()
        batch.update(usersRef.document(uid), "bio", bio)
        batch.set(
            firestore.collection("userContacts").document(uid),
            mapOf("phone" to phone),
            SetOptions.merge()
        )
        batch.commit().awaitResult()
    }

    override suspend fun registerFcmToken(uid: String, token: String) {
        firestore.collection("userDevices").document(uid).set(
            mapOf("fcmTokens" to FieldValue.arrayUnion(token)),
            SetOptions.merge()
        ).awaitResult()
    }
}
