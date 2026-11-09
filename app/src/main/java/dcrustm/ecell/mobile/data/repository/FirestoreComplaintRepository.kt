package dcrustm.ecell.mobile.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import dcrustm.ecell.mobile.data.firebase.awaitResult
import dcrustm.ecell.mobile.data.firebase.snapshots
import dcrustm.ecell.mobile.data.firebase.toComplaint
import dcrustm.ecell.mobile.data.firebase.toFirestoreMap
import dcrustm.ecell.mobile.domain.model.Complaint
import dcrustm.ecell.mobile.domain.model.ComplaintStatus
import dcrustm.ecell.mobile.domain.repository.ComplaintRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FirestoreComplaintRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : ComplaintRepository {

    private val collectionRef get() = firestore.collection("complaints")

    override fun observeAllComplaints(): Flow<List<Complaint>> =
        collectionRef.orderBy("createdAt", Query.Direction.DESCENDING).snapshots().map { snap ->
            snap.documents.mapNotNull { it.toComplaint() }
        }

    override fun observeOwnComplaints(uid: String): Flow<List<Complaint>> =
        collectionRef.whereEqualTo("filerUid", uid).snapshots().map { snap ->
            snap.documents.mapNotNull { it.toComplaint() }.sortedByDescending { it.createdAt }
        }

    override suspend fun submitComplaint(complaint: Complaint) {
        collectionRef.add(
            complaint.toFirestoreMap() + ("createdAt" to FieldValue.serverTimestamp())
        ).awaitResult()
    }

    override suspend fun updateComplaint(id: String, status: ComplaintStatus, response: String) {
        collectionRef.document(id).update(
            mapOf("status" to status.name, "response" to response)
        ).awaitResult()
    }
}
