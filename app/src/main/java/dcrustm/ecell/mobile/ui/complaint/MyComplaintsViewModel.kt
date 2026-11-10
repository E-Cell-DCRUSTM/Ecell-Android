package dcrustm.ecell.mobile.ui.complaint

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dcrustm.ecell.mobile.domain.model.Complaint
import dcrustm.ecell.mobile.domain.repository.ComplaintRepository
import dcrustm.ecell.mobile.session.UserSessionManager
import dcrustm.ecell.mobile.ui.state.ListUiState
import dcrustm.ecell.mobile.ui.state.asListUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class MyComplaintsViewModel @Inject constructor(
    sessionManager: UserSessionManager,
    complaintRepository: ComplaintRepository
) : ViewModel() {

    private val refreshRequest = MutableStateFlow(0)

    val complaintState: StateFlow<ListUiState<Complaint>> = combine(
        sessionManager.currentUser,
        refreshRequest
    ) { user, _ -> user }
        .flatMapLatest { user ->
            if (user == null) {
                flowOf(ListUiState(
                    isLoading = false,
                    errorMessage = "Your profile is unavailable. Sign out and sign in again, then retry."
                ))
            } else {
                complaintRepository.observeOwnComplaints(user.uid)
                    .asListUiState("Could not load your complaints. Please try again.")
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ListUiState())

    fun retry() { refreshRequest.value += 1 }
}
