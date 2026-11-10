package dcrustm.ecell.mobile.ui.coordinator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dcrustm.ecell.mobile.domain.model.Committee
import dcrustm.ecell.mobile.domain.model.Member
import dcrustm.ecell.mobile.domain.model.PendingMemberApplication
import dcrustm.ecell.mobile.domain.repository.ProfilePhotoRepository
import dcrustm.ecell.mobile.domain.model.ROLE
import dcrustm.ecell.mobile.domain.repository.MemberRepository
import dcrustm.ecell.mobile.session.UserSessionManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import dcrustm.ecell.mobile.ui.state.ListUiState
import dcrustm.ecell.mobile.ui.state.asListUiState
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class ManageMembersViewModel @Inject constructor(
    private val sessionManager: UserSessionManager,
    private val memberRepository: MemberRepository,
    private val profilePhotoRepository: ProfilePhotoRepository
) : ViewModel() {

    val currentUser: StateFlow<Member?> = sessionManager.currentUser

    private val refreshRequest = MutableStateFlow(0)
    private val approvedMemberState = refreshRequest.flatMapLatest {
        memberRepository.observeApprovedMembers()
            .asListUiState("Could not load members. Please try again.")
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ListUiState())

    /** Coordinator sees everyone; a committee head only sees their own committee's roster. */
    val allMembersState: StateFlow<ListUiState<Member>> = combine(
        approvedMemberState,
        sessionManager.currentUser
    ) { state, viewer ->
        if (viewer?.role == ROLE.COMMITTEE_HEAD) {
            state.copy(items = state.items.filter { it.committee == viewer.committee })
        } else {
            state
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ListUiState())

    val allMembers: StateFlow<List<Member>> = allMembersState.map { it.items }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingState: StateFlow<ListUiState<PendingMemberApplication>> = combine(
        sessionManager.currentUser,
        refreshRequest
    ) { user, _ -> user }.flatMapLatest { user ->
        if (user?.role == ROLE.COORDINATOR) {
            memberRepository.observePendingApplications()
                .asListUiState("Could not load approval requests. Please try again.")
        } else {
            flowOf(ListUiState(items = emptyList(), isLoading = false))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ListUiState())

    val pendingMembers: StateFlow<List<PendingMemberApplication>> = pendingState.map { it.items }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _pendingPhotos = MutableStateFlow<Map<String, android.graphics.Bitmap>>(emptyMap())
    val pendingPhotos: StateFlow<Map<String, android.graphics.Bitmap>> = _pendingPhotos
    private val photoRequests = mutableSetOf<String>()

    init {
        viewModelScope.launch {
            pendingMembers.collectLatest { applications ->
                val uncached = applications.filter { application ->
                    application.member.profilePhotoUrl.isNotBlank() && application.member.uid !in photoRequests
                }
                if (uncached.isEmpty()) return@collectLatest
                photoRequests += uncached.map { it.member.uid }
                val loaded = coroutineScope {
                    uncached.map { application -> async {
                        application.member.uid to runCatching {
                            profilePhotoRepository.loadProfilePhoto(
                                application.member.uid,
                                application.member.profilePhotoUrl
                            )
                        }.getOrNull()
                    } }.awaitAll()
                }
                _pendingPhotos.value = _pendingPhotos.value + loaded.mapNotNull { (uid, photo) -> photo?.let { uid to it } }
            }
        }
    }

    fun retry() { refreshRequest.value += 1 }

    private val _operationFeedback = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)
    val operationFeedback: StateFlow<String?> = _operationFeedback
    fun clearOperationFeedback() { _operationFeedback.value = null }

    /** Only the coordinator approves/rejects members — heads just view their roster. */
    val canApprove: Boolean get() = sessionManager.isCoordinator

    fun approve(uid: String, name: String, committee: Committee, role: ROLE = ROLE.MEMBER) {
        if (!canApprove) return
        viewModelScope.launch {
            runCatching { memberRepository.approveMember(uid, committee, role) }
                .onSuccess {
                    val roleName = if (role == ROLE.COMMITTEE_HEAD) "committee head" else "member"
                    _operationFeedback.value = "$name approved as $roleName in ${committee.displayName}."
                }
                .onFailure { _operationFeedback.value = it.message ?: "Could not approve this member. Please try again." }
        }
    }

    fun assign(uid: String, name: String, committee: Committee, role: ROLE) {
        if (!canApprove) return
        viewModelScope.launch {
            runCatching { memberRepository.assignMember(uid, committee, role) }
                .onSuccess { _operationFeedback.value = "$name's committee assignment was updated." }
                .onFailure { _operationFeedback.value = it.message ?: "Could not update this member. Please try again." }
        }
    }

    fun reject(uid: String, name: String, message: String = "") {
        if (!canApprove) return
        viewModelScope.launch {
            runCatching { memberRepository.rejectMember(uid, message) }
                .onSuccess {
                    _operationFeedback.value = "$name's application was declined. Their account remains active, and they can sign in to review the note and reapply."
                }
                .onFailure { _operationFeedback.value = it.message ?: "Could not reject this member. Please try again." }
        }
    }

    fun requestCorrection(uid: String, name: String, message: String) {
        if (!canApprove) return
        viewModelScope.launch {
            runCatching { memberRepository.requestProfileCorrection(uid, message) }
                .onSuccess { _operationFeedback.value = "A correction request was sent to $name." }
                .onFailure { _operationFeedback.value = it.message ?: "Could not request a correction. Please try again." }
        }
    }
}
