package dcrustm.ecell.mobile.ui.memberid

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dcrustm.ecell.mobile.domain.model.MemberIdVerification
import dcrustm.ecell.mobile.domain.model.ROLE
import dcrustm.ecell.mobile.domain.repository.MemberRepository
import dcrustm.ecell.mobile.session.UserSessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VerifyMemberIdUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val result: MemberIdVerification? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class MemberIdVerificationViewModel @Inject constructor(
    private val sessionManager: UserSessionManager,
    private val memberRepository: MemberRepository
) : ViewModel() {
    private val _state = MutableStateFlow(VerifyMemberIdUiState())
    val state: StateFlow<VerifyMemberIdUiState> = _state.asStateFlow()

    fun setQuery(value: String) = _state.update { it.copy(query = value.take(80), errorMessage = null) }

    fun verify(rawValue: String = _state.value.query) {
        if (sessionManager.currentUser.value?.role != ROLE.COORDINATOR) {
            _state.update { it.copy(result = null, errorMessage = "Coordinator access is required to verify an ID.") }
            return
        }
        val memberId = MemberIdPayload.decode(rawValue)
        if (memberId == null) {
            _state.update { it.copy(result = null, errorMessage = "Enter a valid E-Cell member ID or scan its QR code.") }
            return
        }
        _state.update { it.copy(query = memberId, isLoading = true, result = null, errorMessage = null) }
        viewModelScope.launch {
            runCatching { memberRepository.verifyMemberId(memberId) }
                .onSuccess { result ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            result = result,
                            errorMessage = if (result == null) "No approved member was found for this ID." else null
                        )
                    }
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false,
                        errorMessage = error.message ?: "Could not verify this ID. Check the connection and retry.") }
                }
        }
    }
}
