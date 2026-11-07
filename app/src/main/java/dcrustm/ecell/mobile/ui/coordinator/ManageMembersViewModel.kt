package dcrustm.ecell.mobile.ui.coordinator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dcrustm.ecell.mobile.domain.model.Committee
import dcrustm.ecell.mobile.domain.model.Member
import dcrustm.ecell.mobile.domain.model.ROLE
import dcrustm.ecell.mobile.domain.repository.MemberRepository
import dcrustm.ecell.mobile.session.UserSessionManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class ManageMembersViewModel @Inject constructor(
    private val sessionManager: UserSessionManager,
    private val memberRepository: MemberRepository
) : ViewModel() {

    val currentUser: StateFlow<Member?> = sessionManager.currentUser

    /** Coordinator sees everyone; a committee head only sees their own committee's roster. */
    val allMembers: StateFlow<List<Member>> = combine(
        memberRepository.observeApprovedMembers(),
        sessionManager.currentUser
    ) { members, viewer ->
        if (viewer?.role == ROLE.COMMITTEE_HEAD) {
            members.filter { it.committee == viewer.committee }
        } else {
            members
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingMembers: StateFlow<List<Member>> = sessionManager.currentUser.flatMapLatest { user ->
        if (user?.role == ROLE.COORDINATOR) memberRepository.observePendingMembers()
        else flowOf(emptyList())
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _operationError = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)
    val operationError: StateFlow<String?> = _operationError
    fun clearOperationError() { _operationError.value = null }

    /** Only the coordinator approves/rejects members — heads just view their roster. */
    val canApprove: Boolean get() = sessionManager.isCoordinator

    fun approve(uid: String, committee: Committee, role: ROLE = ROLE.MEMBER) {
        if (!canApprove) return
        viewModelScope.launch {
            runCatching { memberRepository.approveMember(uid, committee, role) }
                .onFailure { _operationError.value = it.message ?: "Could not approve this member. Please try again." }
        }
    }

    fun reject(uid: String) {
        if (!canApprove) return
        viewModelScope.launch {
            runCatching { memberRepository.rejectMember(uid) }
                .onFailure { _operationError.value = it.message ?: "Could not reject this member. Please try again." }
        }
    }
}
