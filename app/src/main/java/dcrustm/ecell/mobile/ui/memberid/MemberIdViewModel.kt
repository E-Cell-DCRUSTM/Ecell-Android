package dcrustm.ecell.mobile.ui.memberid

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dcrustm.ecell.mobile.domain.model.Member
import dcrustm.ecell.mobile.domain.repository.MemberRepository
import dcrustm.ecell.mobile.domain.repository.ProfilePhotoRepository
import dcrustm.ecell.mobile.domain.model.ROLE
import dcrustm.ecell.mobile.session.UserSessionManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MemberIdUiState(
    val isLoading: Boolean = true,
    val member: Member? = null,
    val memberId: String = "",
    val card: Bitmap? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class MemberIdViewModel @Inject constructor(
    private val sessionManager: UserSessionManager,
    private val memberRepository: MemberRepository,
    private val photoRepository: ProfilePhotoRepository
) : ViewModel() {
    private val _state = MutableStateFlow(MemberIdUiState())
    val state: StateFlow<MemberIdUiState> = _state.asStateFlow()
    private var observationJob: Job? = null

    init {
        val member = sessionManager.currentUser.value
        if (member == null || !member.isApproved || member.role == ROLE.PENDING) {
            _state.value = MemberIdUiState(isLoading = false, errorMessage = "A digital E-Cell ID is available after coordinator approval.")
        } else {
            observeApprovedMember(member)
        }
    }

    private fun observeApprovedMember(member: Member) {
        observationJob?.cancel()
        observationJob = viewModelScope.launch {
            val photo = member.profilePhotoUrl.takeIf { it.isNotBlank() }
                ?.let { photoRepository.loadProfilePhoto(member.uid, it) }
            var issuingId = false
            memberRepository.observeApplication(member.uid).collect { application ->
                val existing = application?.memberId.orEmpty()
                if (existing.isNotBlank()) {
                    _state.value = MemberIdUiState(
                        isLoading = false,
                        member = member,
                        memberId = existing,
                        card = renderMemberIdCard(member, existing, photo)
                    )
                } else if (!issuingId) {
                    issuingId = true
                    _state.value = MemberIdUiState(isLoading = true, member = member)
                    runCatching { memberRepository.issueMemberId(member.uid) }
                        .onSuccess { issued ->
                            _state.value = MemberIdUiState(
                                isLoading = false,
                                member = member,
                                memberId = issued,
                                card = renderMemberIdCard(member, issued, photo)
                            )
                        }
                        .onFailure { error ->
                            _state.value = MemberIdUiState(
                                isLoading = false,
                                member = member,
                                errorMessage = error.message ?: "Could not prepare your member ID. Please retry."
                            )
                        }
                }
            }
        }
    }

    fun retry() {
        _state.value = MemberIdUiState()
        val member = sessionManager.currentUser.value
        if (member != null && member.isApproved && member.role != ROLE.PENDING) {
            observeApprovedMember(member)
        } else {
            observationJob?.cancel()
            _state.value = MemberIdUiState(
                isLoading = false,
                errorMessage = "A digital E-Cell ID is available after coordinator approval."
            )
        }
    }
}
