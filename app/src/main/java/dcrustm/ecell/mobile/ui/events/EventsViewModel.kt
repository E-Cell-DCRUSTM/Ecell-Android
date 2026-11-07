package dcrustm.ecell.mobile.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dcrustm.ecell.mobile.domain.model.EventItem
import dcrustm.ecell.mobile.domain.model.Member
import dcrustm.ecell.mobile.domain.repository.EventRepository
import dcrustm.ecell.mobile.session.UserSessionManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class EventsViewModel @Inject constructor(
    private val sessionManager: UserSessionManager,
    eventRepository: EventRepository
) : ViewModel() {

    val currentUser: StateFlow<Member?> = sessionManager.currentUser

    val canCreate: Boolean get() = sessionManager.isCoordinator || sessionManager.isHead

    val upcomingEvents: StateFlow<List<EventItem>> = combine(
        eventRepository.observeEvents(),
        sessionManager.currentUser
    ) { all, user ->
        val visible = when {
            user == null -> emptyList()
            sessionManager.isCoordinator -> all
            else -> all.filter { it.isOrgWide || it.committee == user.committee }
        }
        visible.filter { !it.isPast }.sortedBy { it.startTime }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
