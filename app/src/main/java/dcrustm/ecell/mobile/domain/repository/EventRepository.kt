package dcrustm.ecell.mobile.domain.repository

import dcrustm.ecell.mobile.domain.model.EventItem
import dcrustm.ecell.mobile.domain.model.Member
import kotlinx.coroutines.flow.Flow

interface EventRepository {

    fun observeEvents(member: Member): Flow<List<EventItem>>

    suspend fun createEvent(event: EventItem)

    suspend fun deleteEvent(id: String)
}
