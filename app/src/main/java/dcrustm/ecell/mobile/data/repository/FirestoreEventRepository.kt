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
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FirestoreEventRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : EventRepository {

    private val collectionRef get() = firestore.collection("events")

    override fun observeEvents(): Flow<List<EventItem>> =
        collectionRef.orderBy("startTime", Query.Direction.ASCENDING).snapshots().map { snap ->
            snap.documents.mapNotNull { it.toEvent() }
        }

    override suspend fun createEvent(event: EventItem) {
        collectionRef.add(event.toFirestoreMap()).awaitResult()
    }

    override suspend fun deleteEvent(id: String) {
        collectionRef.document(id).delete().awaitResult()
    }
}
