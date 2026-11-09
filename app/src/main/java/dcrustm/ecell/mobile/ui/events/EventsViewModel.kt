package dcrustm.ecell.mobile.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dcrustm.ecell.mobile.domain.model.EventItem
import dcrustm.ecell.mobile.domain.model.Member
import dcrustm.ecell.mobile.domain.repository.EventRepository
import dcrustm.ecell.mobile.session.UserSessionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class EventsViewModel @Inject constructor(
    private val sessionManager: UserSessionManager,
    eventRepository: EventRepository
) : ViewModel() {

    val currentUser: StateFlow<Member?> = sessionManager.currentUser

    val canCreate: Boolean get() = sessionManager.isCoordinator || sessionManager.isHead

    val upcomingEvents: StateFlow<List<EventItem>> = sessionManager.currentUser
        .flatMapLatest { user ->
            if (user?.isApproved == true) eventRepository.observeEvents(user)
            else flowOf(emptyList())
        }
        .map { events -> events.filterNot { it.isPast }.sortedBy { it.startTime } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
