package dcrustm.ecell.mobile.domain.repository

import dcrustm.ecell.mobile.domain.model.Complaint
import dcrustm.ecell.mobile.domain.model.ComplaintStatus
import kotlinx.coroutines.flow.Flow

interface ComplaintRepository {

    /** Coordinator-only: every complaint, with filer identity. */
    fun observeAllComplaints(): Flow<List<Complaint>>

    /** A member's own filed complaints (status + response, no identity concern — it's their own). */
    fun observeOwnComplaints(uid: String): Flow<List<Complaint>>

    suspend fun submitComplaint(complaint: Complaint)

    suspend fun updateComplaint(id: String, status: ComplaintStatus, response: String)
}
