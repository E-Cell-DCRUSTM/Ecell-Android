package dcrustm.ecell.mobile.ui.complaint

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dcrustm.ecell.mobile.domain.model.Complaint
import dcrustm.ecell.mobile.domain.repository.ComplaintRepository
import dcrustm.ecell.mobile.session.UserSessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ComplaintViewModel @Inject constructor(
    private val sessionManager: UserSessionManager,
    private val complaintRepository: ComplaintRepository
) : ViewModel() {

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun clearError() { _errorMessage.value = null }

    fun submit(title: String, body: String, onDone: () -> Unit) {
        val user = sessionManager.currentUser.value ?: return
        if (_isSubmitting.value) return
        _isSubmitting.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                complaintRepository.submitComplaint(
                    Complaint(
                        title = title,
                        body = body,
                        filerUid = user.uid,
                        filerName = user.name,
                        filerCommittee = user.committee
                    )
                )
                onDone()
            } catch (error: Exception) {
                _errorMessage.value = error.message ?: "Could not submit the complaint. Please try again."
            } finally {
                _isSubmitting.value = false
            }
        }
    }
}
