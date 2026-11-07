package dcrustm.ecell.mobile.domain.repository

import dcrustm.ecell.mobile.domain.model.Committee
import dcrustm.ecell.mobile.domain.model.Member
import dcrustm.ecell.mobile.domain.model.MemberContact
import dcrustm.ecell.mobile.domain.model.ROLE
import kotlinx.coroutines.flow.Flow

interface MemberRepository {

    /** Approved members only — the directory. */
    fun observeApprovedMembers(): Flow<List<Member>>

    /** Members awaiting coordinator approval. */
    fun observePendingMembers(): Flow<List<Member>>

    fun observeMember(uid: String): Flow<Member?>
    fun observeContact(uid: String): Flow<MemberContact?>

    suspend fun approveMember(uid: String, committee: Committee, role: ROLE)

    suspend fun rejectMember(uid: String)

    suspend fun updateOwnProfile(uid: String, phone: String, bio: String)

    /** Adds this device's FCM token to the member's token list (for push targeting). */
    suspend fun registerFcmToken(uid: String, token: String)
}
