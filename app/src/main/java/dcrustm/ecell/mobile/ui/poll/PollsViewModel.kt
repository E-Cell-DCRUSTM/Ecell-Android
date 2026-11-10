package dcrustm.ecell.mobile.ui.poll

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dcrustm.ecell.mobile.domain.model.Member
import dcrustm.ecell.mobile.domain.model.Poll
import dcrustm.ecell.mobile.domain.model.ROLE
import dcrustm.ecell.mobile.domain.repository.PollRepository
import dcrustm.ecell.mobile.session.UserSessionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import dcrustm.ecell.mobile.ui.state.ListUiState
import dcrustm.ecell.mobile.ui.state.asListUiState
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class PollsViewModel @Inject constructor(
    private val pollRepository: PollRepository,
    private val session: UserSessionManager
) : ViewModel() {

    val currentUser: StateFlow<Member?> = session.currentUser
    private val refreshRequest = MutableStateFlow(0)

    val pollState: StateFlow<ListUiState<Poll>> = combine(session.currentUser, refreshRequest) { user, _ -> user }
        .flatMapLatest { member ->
            if (member?.isApproved == true) pollRepository.observePolls(member)
            else flowOf(emptyList())
        }
        .asListUiState("Could not load polls. Please try again.")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ListUiState())

    fun retry() { refreshRequest.value += 1 }

    val canCreate: Boolean
        get() = session.isCoordinator || session.isHead

    fun canClose(poll: Poll): Boolean = session.isCoordinator ||
        (session.isHead && poll.createdByUid == session.currentUser.value?.uid)

    fun create(title: String, question: String, options: List<String>, audience: String, onDone: () -> Unit) {
        val creator = session.currentUser.value ?: return
        viewModelScope.launch {
            runCatching {
                pollRepository.createPoll(title, question, options, audience, creator)
            }.onSuccess { onDone() }.onFailure { /* Screen reports through the action state below. */
                _error.value = it.message ?: "Could not create the poll."
            }
        }
    }

    private val _error = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    fun clearError() { _error.value = null }

    fun vote(pollId: String, optionIndex: Int) {
        val uid = session.currentUser.value?.uid ?: return
        viewModelScope.launch {
            runCatching { pollRepository.castVote(pollId, optionIndex, uid) }
                .onFailure { _error.value = it.message ?: "Your vote could not be saved." }
        }
    }

    fun close(pollId: String) {
        viewModelScope.launch {
            runCatching { pollRepository.closePoll(pollId) }
                .onFailure { _error.value = it.message ?: "Could not close the poll." }
        }
    }
}
