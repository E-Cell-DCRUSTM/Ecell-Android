package dcrustm.ecell.mobile.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dcrustm.ecell.mobile.data.firebase.awaitResult
import dcrustm.ecell.mobile.data.firebase.snapshots
import dcrustm.ecell.mobile.data.firebase.toAnnouncement
import dcrustm.ecell.mobile.data.firebase.toFirestoreMap
import dcrustm.ecell.mobile.domain.model.Announcement
import dcrustm.ecell.mobile.domain.repository.AnnouncementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import dcrustm.ecell.mobile.domain.model.Member
import dcrustm.ecell.mobile.domain.model.ROLE
import javax.inject.Inject

class FirestoreAnnouncementRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : AnnouncementRepository {

    private val collectionRef get() = firestore.collection("announcements")

    override fun observeAnnouncements(member: Member): Flow<List<Announcement>> {
        if (member.role == ROLE.COORDINATOR) {
            return collectionRef.orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots().map { snap -> snap.documents.mapNotNull { it.toAnnouncement() } }
        }

        val committee = collectionRef.whereEqualTo("committee", member.committee.name).snapshots()
        val organization = collectionRef.whereEqualTo("committee", null).snapshots()
        return combine(committee, organization) { committeeSnap, organizationSnap ->
            (committeeSnap.documents + organizationSnap.documents)
                .distinctBy { it.id }
                .mapNotNull { it.toAnnouncement() }
                .sortedByDescending { it.createdAt }
        }
    }

    override suspend fun postAnnouncement(announcement: Announcement) {
        collectionRef.add(announcement.toFirestoreMap()).awaitResult()
    }
}
