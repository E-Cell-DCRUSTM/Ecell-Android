package dcrustm.ecell.mobile.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dcrustm.ecell.mobile.domain.model.Announcement
import dcrustm.ecell.mobile.domain.model.Member
import dcrustm.ecell.mobile.domain.repository.AnnouncementRepository
import dcrustm.ecell.mobile.session.UserSessionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import dcrustm.ecell.mobile.ui.state.ListUiState
import dcrustm.ecell.mobile.ui.state.asListUiState
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel @Inject constructor(
    private val sessionManager: UserSessionManager,
    announcementRepository: AnnouncementRepository
) : ViewModel() {

    val currentUser: StateFlow<Member?> = sessionManager.currentUser

    private val refreshRequest = MutableStateFlow(0)

    val announcementState: StateFlow<ListUiState<Announcement>> = combine(
        sessionManager.currentUser,
        refreshRequest
    ) { user, _ -> user }
        .flatMapLatest { user ->
            if (user?.isApproved == true) announcementRepository.observeAnnouncements(user)
            else flowOf(emptyList())
        }
        .map { announcements -> announcements.sortedWith(
            compareByDescending<Announcement> { it.isPinned }.thenByDescending { it.createdAt }
        ) }
        .asListUiState("Could not load announcements. Please try again.")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ListUiState())

    fun retry() { refreshRequest.value += 1 }
}
