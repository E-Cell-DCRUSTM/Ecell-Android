package dcrustm.ecell.mobile.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dcrustm.ecell.mobile.domain.model.Member
import dcrustm.ecell.mobile.domain.repository.MemberRepository
import dcrustm.ecell.mobile.session.UserSessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val sessionManager: UserSessionManager,
    private val memberRepository: MemberRepository
) : ViewModel() {

    val currentUser: StateFlow<Member?> = sessionManager.currentUser

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    fun clearError() { _errorMessage.value = null }

    fun updateProfile(phone: String, bio: String) {
        val uid = sessionManager.currentUser.value?.uid ?: return
        if (_isSaving.value) return
        _isSaving.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                memberRepository.updateOwnProfile(uid, phone, bio)
            } catch (error: Exception) {
                _errorMessage.value = error.message ?: "Could not save your profile. Please try again."
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun signOut() = sessionManager.signOut()
}
