package dcrustm.ecell.mobile.data.repository

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.auth.FirebaseAuth
import dcrustm.ecell.mobile.data.firebase.awaitResult
import dcrustm.ecell.mobile.data.firebase.snapshots
import dcrustm.ecell.mobile.data.firebase.toMember
import dcrustm.ecell.mobile.data.firebase.toMemberApplication
import dcrustm.ecell.mobile.domain.model.Committee
import dcrustm.ecell.mobile.domain.model.Member
import dcrustm.ecell.mobile.domain.model.MemberApplication
import dcrustm.ecell.mobile.domain.model.MemberContact
import dcrustm.ecell.mobile.domain.model.MemberIdVerification
import dcrustm.ecell.mobile.domain.model.MembershipApplicationStatus
import dcrustm.ecell.mobile.domain.model.PendingMemberApplication
import dcrustm.ecell.mobile.domain.model.ProfileDraft
import dcrustm.ecell.mobile.domain.model.ROLE
import dcrustm.ecell.mobile.domain.repository.MemberRepository
import dcrustm.ecell.mobile.domain.validation.ProfileDraftValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

class FirestoreMemberRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : MemberRepository {

    private val usersRef get() = firestore.collection("users")
    private val contactsRef get() = firestore.collection("userContacts")
    private val applicationsRef get() = firestore.collection("memberApplications")
    private val memberIdIndexRef get() = firestore.collection("memberIdIndex")

    override fun observeApprovedMembers(): Flow<List<Member>> =
        usersRef.whereEqualTo("isApproved", true).snapshots().map { snap ->
            snap.documents.mapNotNull { it.toMember() }
        }

    override fun observePendingMembers(): Flow<List<Member>> =
        observePendingApplications().map { applications -> applications.map { it.member } }

    override fun observePendingApplications(): Flow<List<PendingMemberApplication>> = combine(
        usersRef.whereEqualTo("isApproved", false).snapshots(),
        contactsRef.snapshots(),
        applicationsRef.snapshots()
    ) { users, contacts, applications ->
        val contactsByUid = contacts.documents.associateBy { it.id }
        val applicationsByUid = applications.documents.associateBy { it.id }
        users.documents.mapNotNull { document ->
            val member = document.toMember() ?: return@mapNotNull null
            // Older profiles predate application documents; keep them in the
            // coordinator queue until they are reviewed.
            val application = applicationsByUid[member.uid]?.toMemberApplication(member.uid)
                ?: MemberApplication(
                    uid = member.uid,
                    status = MembershipApplicationStatus.PENDING,
                    profileCompleted = true
                )
            if (application.status !in setOf(
                    MembershipApplicationStatus.PENDING,
                    MembershipApplicationStatus.NEEDS_CORRECTION
                )
            ) return@mapNotNull null

            val contact = contactsByUid[member.uid]
            val enrichedMember = member.copy(
                email = contact?.getString("email").orEmpty(),
                phone = contact?.getString("phone").orEmpty()
            )
            PendingMemberApplication(enrichedMember, application)
        }
    }

    override fun observeMember(uid: String): Flow<Member?> =
        usersRef.document(uid).snapshots().map { it.toMember() }

    override fun observeContact(uid: String): Flow<MemberContact?> =
        contactsRef.document(uid).snapshots().map { snapshot ->
            if (!snapshot.exists()) null else MemberContact(
                email = snapshot.getString("email") ?: "",
                phone = snapshot.getString("phone") ?: ""
            )
        }

    override fun observeApplication(uid: String): Flow<MemberApplication?> =
        applicationsRef.document(uid).snapshots().map { it.toMemberApplication(uid) }

    override suspend fun saveOnboardingDraft(uid: String, draft: ProfileDraft) {
        val errors = ProfileDraftValidator.validateDraft(draft, uid)
        require(errors.isEmpty()) { errors.values.firstOrNull() ?: "Check the profile details." }
        val batch = firestore.batch()
        batch.update(usersRef.document(uid), mapOf(
            "name" to draft.name.trim(),
            "year" to draft.year,
            "branch" to draft.branch.trim(),
            "bio" to draft.bio.trim(),
            "profilePhotoUrl" to draft.profilePhotoPath
        ))
        batch.update(contactsRef.document(uid), "phone", draft.phone.trim())
        batch.update(applicationsRef.document(uid), mapOf(
            "section" to draft.section.trim(),
            "enrollmentNumber" to draft.enrollmentNumber.trim(),
            "preferredCommittee" to draft.preferredCommittee.name,
            "activeStep" to draft.activeStep,
            "updatedAt" to FieldValue.serverTimestamp()
        ))
        batch.commit().awaitResult()
    }

    override suspend fun submitOnboardingProfile(uid: String, draft: ProfileDraft) {
        val errors = ProfileDraftValidator.validate(draft, uid)
        require(errors.isEmpty()) { errors.values.firstOrNull() ?: "Check the profile details." }
        // Save profile data first, then mark the already-persisted profile as
        // submitted. If the second write fails, the user can safely retry.
        saveOnboardingDraft(uid, draft.copy(activeStep = 5))
        applicationsRef.document(uid).update(mapOf(
            "profileCompleted" to true,
            "applicationStatus" to MembershipApplicationStatus.PENDING.name,
            "feedback" to "",
            "activeStep" to 5,
            "submittedAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )).awaitResult()
    }

    override suspend fun reapplyForMembership(uid: String) {
        applicationsRef.document(uid).update(mapOf(
            "applicationStatus" to MembershipApplicationStatus.INCOMPLETE.name,
            "profileCompleted" to false,
            "feedback" to "",
            "activeStep" to 1,
            "submittedAt" to null,
            "updatedAt" to FieldValue.serverTimestamp()
        )).awaitResult()
    }

    override suspend fun requestProfileCorrection(uid: String, message: String) {
        val feedback = message.trim().take(500)
        require(feedback.isNotBlank()) { "Add a short note describing the correction needed." }
        val applicationRef = applicationsRef.document(uid)
        firestore.runTransaction { transaction ->
            val user = transaction.get(usersRef.document(uid))
            require(user.exists() && user.getBoolean("isApproved") != true) {
                "Only a pending application can be returned for correction."
            }
            val application = transaction.get(applicationRef)
            val data = applicationData(application).apply {
                put("applicationStatus", MembershipApplicationStatus.NEEDS_CORRECTION.name)
                put("profileCompleted", false)
                put("feedback", feedback)
                put("updatedAt", FieldValue.serverTimestamp())
            }
            transaction.set(applicationRef, data)
            addAudit(transaction, uid, "CORRECTION_REQUESTED", feedback)
            null
        }.awaitResult()
    }

    override suspend fun rejectMember(uid: String, message: String) {
        val feedback = message.trim().take(500)
        val applicationRef = applicationsRef.document(uid)
        firestore.runTransaction { transaction ->
            val user = transaction.get(usersRef.document(uid))
            require(user.exists() && user.getBoolean("isApproved") != true) {
                "Only a pending application can be declined."
            }
            val application = transaction.get(applicationRef)
            require(application.getString("applicationStatus") != MembershipApplicationStatus.INCOMPLETE.name) {
                "This member has not submitted an application yet."
            }
            val data = applicationData(application).apply {
                put("applicationStatus", MembershipApplicationStatus.REJECTED.name)
                put("profileCompleted", true)
                put("feedback", feedback)
                put("memberId", "")
                put("updatedAt", FieldValue.serverTimestamp())
            }
            transaction.set(applicationRef, data)
            addAudit(transaction, uid, "REJECTED", feedback)
            null
        }.awaitResult()
    }

    override suspend fun approveMember(uid: String, committee: Committee, role: ROLE) {
        require(committee != Committee.NONE) { "Choose a committee before approving this member." }
        require(role == ROLE.MEMBER || role == ROLE.COMMITTEE_HEAD) {
            "A coordinator role must be granted outside the app."
        }
        allocateMemberId(uid, committee, role, approve = true)
    }

    override suspend fun issueMemberId(uid: String): String =
        allocateMemberId(uid, committee = null, role = null, approve = false)

    private suspend fun allocateMemberId(
        uid: String,
        committee: Committee?,
        role: ROLE?,
        approve: Boolean
    ): String {
        repeat(MAX_MEMBER_ID_COLLISION_RETRIES) {
            val candidate = newMemberId()
            val result = firestore.runTransaction { transaction ->
                val userRef = usersRef.document(uid)
                val applicationRef = applicationsRef.document(uid)
                val user = transaction.get(userRef)
                require(user.exists()) { "Member profile not found." }
                if (approve) {
                    require(user.getBoolean("isApproved") != true) { "This account is already approved." }
                } else {
                    require(user.getBoolean("isApproved") == true) { "Only an approved member can receive an ID." }
                }
                val application = transaction.get(applicationRef)
                val existingId = application.getString("memberId").orEmpty()
                if (existingId.isNotBlank()) {
                    val existingIndex = transaction.get(memberIdIndexRef.document(existingId))
                    check(existingIndex.getString("uid") == uid) {
                        "The saved member ID could not be verified. Contact the coordinator."
                    }
                    return@runTransaction existingId
                }
                if (approve && application.exists()) {
                    require(application.getString("applicationStatus") == MembershipApplicationStatus.PENDING.name) {
                        "This application must be submitted before it can be approved."
                    }
                }

                val indexRef = memberIdIndexRef.document(candidate)
                if (transaction.get(indexRef).exists()) return@runTransaction null

                if (approve) {
                    val contactRef = contactsRef.document(uid)
                    transaction.get(contactRef)
                    transaction.update(userRef, mapOf(
                        "isApproved" to true,
                        "committee" to committee!!.name,
                        "role" to role!!.name
                    ))
                    transaction.set(contactRef, mapOf("committee" to committee.name), SetOptions.merge())
                }

                val applicationData = applicationData(application).apply {
                    put("applicationStatus", MembershipApplicationStatus.APPROVED.name)
                    put("profileCompleted", true)
                    put("feedback", "")
                    put("memberId", candidate)
                    put("updatedAt", FieldValue.serverTimestamp())
                    if (get("submittedAt") == null) put("submittedAt", FieldValue.serverTimestamp())
                }
                transaction.set(applicationRef, applicationData)
                transaction.set(indexRef, mapOf(
                    "uid" to uid,
                    "createdAt" to FieldValue.serverTimestamp()
                ))
                if (approve) {
                    addAudit(
                        transaction = transaction,
                        uid = uid,
                        action = "APPROVED",
                        committee = committee?.name.orEmpty(),
                        role = role?.name.orEmpty(),
                        memberId = candidate
                    )
                }
                candidate
            }.awaitResult()
            if (result != null) return result
        }
        error("Could not allocate a unique E-Cell member ID. Please retry.")
    }

    override suspend fun verifyMemberId(memberId: String): MemberIdVerification? {
        val normalized = normalizeMemberId(memberId) ?: return null
        val index = memberIdIndexRef.document(normalized).get().awaitResult()
        val uid = index.getString("uid") ?: return null
        val memberSnapshot = usersRef.document(uid).get().awaitResult()
        val member = memberSnapshot.toMember()?.takeIf { it.isApproved } ?: return null
        return MemberIdVerification(
            memberId = normalized,
            name = member.name,
            branch = member.branch,
            year = member.year,
            committee = member.committee,
            role = member.role
        )
    }

    override suspend fun assignMember(uid: String, committee: Committee, role: ROLE) {
        require(committee != Committee.NONE) { "Choose a committee for this member." }
        require(role == ROLE.MEMBER || role == ROLE.COMMITTEE_HEAD) {
            "Coordinator roles cannot be assigned from this screen."
        }
        firestore.runTransaction { transaction ->
            val userRef = usersRef.document(uid)
            val user = transaction.get(userRef)
            require(user.exists() && user.getBoolean("isApproved") == true) {
                "Only an approved member can be reassigned."
            }
            val contactRef = contactsRef.document(uid)
            transaction.get(contactRef)
            transaction.update(userRef, mapOf(
                "isApproved" to true,
                "committee" to committee.name,
                "role" to role.name
            ))
            transaction.set(contactRef, mapOf("committee" to committee.name), SetOptions.merge())
            addAudit(transaction, uid, "ASSIGNMENT_UPDATED", committee = committee.name, role = role.name)
            null
        }.awaitResult()
    }

    override suspend fun updateOwnProfile(uid: String, name: String, phone: String, bio: String, profilePhotoPath: String) {
        require(name.trim().length in 2..100) { "Enter your name (2–100 characters)." }
        require(ProfileDraftValidator.isValidPhone(phone)) {
            "Enter a valid phone number with 7–15 digits, or leave it blank."
        }
        require(bio.length <= 1000) { "Introduction must be 1,000 characters or fewer." }
        require(ProfileDraftValidator.validatePhotoPath(profilePhotoPath, uid)) {
            "Choose a profile photo uploaded from this account."
        }
        val batch = firestore.batch()
        batch.update(usersRef.document(uid), mapOf(
            "name" to name.trim(),
            "bio" to bio.trim(),
            "profilePhotoUrl" to profilePhotoPath
        ))
        batch.set(contactsRef.document(uid), mapOf("phone" to phone), SetOptions.merge())
        batch.commit().awaitResult()
    }

    override suspend fun registerFcmToken(uid: String, token: String) {
        firestore.collection("userDevices").document(uid).set(
            mapOf("fcmTokens" to FieldValue.arrayUnion(token)),
            SetOptions.merge()
        ).awaitResult()
    }

    private fun applicationData(snapshot: DocumentSnapshot): MutableMap<String, Any?> {
        if (snapshot.exists()) return snapshot.data?.toMutableMap() ?: mutableMapOf()
        return mutableMapOf(
            "enrollmentNumber" to "",
            "section" to "",
            "preferredCommittee" to Committee.NONE.name,
            "profileCompleted" to true,
            "applicationStatus" to MembershipApplicationStatus.PENDING.name,
            "feedback" to "",
            "activeStep" to 5,
            "memberId" to "",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
            "submittedAt" to null
        )
    }

    private fun addAudit(
        transaction: com.google.firebase.firestore.Transaction,
        uid: String,
        action: String,
        message: String = "",
        committee: String = "",
        role: String = "",
        memberId: String = ""
    ) {
        val event = firestore.collection("memberApplicationAudit")
            .document(uid).collection("events").document()
        transaction.set(event, mapOf(
            "uid" to uid,
            "actorUid" to checkNotNull(firebaseAuth.currentUser?.uid) {
                "Sign in again before changing membership status."
            },
            "action" to action,
            "message" to message,
            "committee" to committee,
            "role" to role,
            "memberId" to memberId,
            "at" to FieldValue.serverTimestamp()
        ))
    }

    private fun newMemberId(): String = "EC-${UUID.randomUUID().toString().replace("-", "")
        .take(16).uppercase(Locale.ROOT)}"

    private fun normalizeMemberId(value: String): String? {
        val normalized = value.trim().uppercase(Locale.ROOT).removePrefix("ECELL-ID:")
        return normalized.takeIf { MEMBER_ID_PATTERN.matches(it) }
    }

    private companion object {
        const val MAX_MEMBER_ID_COLLISION_RETRIES = 8
        val MEMBER_ID_PATTERN = Regex("EC-[0-9A-F]{16}")
    }
}
