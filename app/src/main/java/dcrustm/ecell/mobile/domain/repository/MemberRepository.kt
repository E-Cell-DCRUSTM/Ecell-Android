package dcrustm.ecell.mobile.domain.repository

import dcrustm.ecell.mobile.domain.model.Committee
import dcrustm.ecell.mobile.domain.model.Member
import dcrustm.ecell.mobile.domain.model.MemberContact
import dcrustm.ecell.mobile.domain.model.MemberApplication
import dcrustm.ecell.mobile.domain.model.MemberIdVerification
import dcrustm.ecell.mobile.domain.model.PendingMemberApplication
import dcrustm.ecell.mobile.domain.model.ProfileDraft
import dcrustm.ecell.mobile.domain.model.ROLE
import kotlinx.coroutines.flow.Flow

interface MemberRepository {

    /** Approved members only — the directory. */
    fun observeApprovedMembers(): Flow<List<Member>>

    /** Members awaiting coordinator approval. */
    fun observePendingMembers(): Flow<List<Member>>

    fun observeMember(uid: String): Flow<Member?>
    fun observeContact(uid: String): Flow<MemberContact?>
    fun observeApplication(uid: String): Flow<MemberApplication?>
    fun observePendingApplications(): Flow<List<PendingMemberApplication>>

    suspend fun saveOnboardingDraft(uid: String, draft: ProfileDraft)
    suspend fun submitOnboardingProfile(uid: String, draft: ProfileDraft)
    suspend fun reapplyForMembership(uid: String)
    suspend fun requestProfileCorrection(uid: String, message: String)
    suspend fun rejectMember(uid: String, message: String)
    suspend fun issueMemberId(uid: String): String
    suspend fun verifyMemberId(memberId: String): MemberIdVerification?

    suspend fun approveMember(uid: String, committee: Committee, role: ROLE)

    /** Coordinator-only assignment of an already approved non-coordinator member. */
    suspend fun assignMember(uid: String, committee: Committee, role: ROLE)

    suspend fun updateOwnProfile(uid: String, name: String, phone: String, bio: String, profilePhotoPath: String)

    /** Adds this device's FCM token to the member's token list (for push targeting). */
    suspend fun registerFcmToken(uid: String, token: String)
}
