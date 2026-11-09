package dcrustm.ecell.mobile.domain.repository

import dcrustm.ecell.mobile.domain.model.Announcement
import dcrustm.ecell.mobile.domain.model.Member
import kotlinx.coroutines.flow.Flow

interface AnnouncementRepository {

    fun observeAnnouncements(member: Member): Flow<List<Announcement>>

    suspend fun postAnnouncement(announcement: Announcement)
}
