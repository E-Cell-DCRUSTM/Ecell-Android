package dcrustm.ecell.mobile.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dcrustm.ecell.mobile.data.firebase.awaitResult
import dcrustm.ecell.mobile.data.firebase.snapshots
import dcrustm.ecell.mobile.data.firebase.toEvent
import dcrustm.ecell.mobile.data.firebase.toFirestoreMap
import dcrustm.ecell.mobile.domain.model.EventItem
import dcrustm.ecell.mobile.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import dcrustm.ecell.mobile.domain.model.Member
import dcrustm.ecell.mobile.domain.model.ROLE
import javax.inject.Inject

class FirestoreEventRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : EventRepository {

    private val collectionRef get() = firestore.collection("events")

    override fun observeEvents(member: Member): Flow<List<EventItem>> {
        if (member.role == ROLE.COORDINATOR) {
            return collectionRef.orderBy("startTime", Query.Direction.ASCENDING)
                .snapshots().map { snap -> snap.documents.mapNotNull { it.toEvent() } }
        }

        val committee = collectionRef.whereEqualTo("committee", member.committee.name).snapshots()
        val organization = collectionRef.whereEqualTo("committee", null).snapshots()
        return combine(committee, organization) { committeeSnap, organizationSnap ->
            (committeeSnap.documents + organizationSnap.documents)
                .distinctBy { it.id }
                .mapNotNull { it.toEvent() }
                .sortedBy { it.startTime }
        }
    }

    override suspend fun createEvent(event: EventItem) {
        collectionRef.add(event.toFirestoreMap()).awaitResult()
    }

    override suspend fun deleteEvent(id: String) {
        collectionRef.document(id).delete().awaitResult()
    }
}
