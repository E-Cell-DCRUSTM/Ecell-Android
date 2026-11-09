package dcrustm.ecell.mobile.ui.complaint

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dcrustm.ecell.mobile.domain.model.Complaint
import dcrustm.ecell.mobile.domain.model.ComplaintStatus
import dcrustm.ecell.mobile.domain.repository.ComplaintRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.flatMapLatest
import dcrustm.ecell.mobile.ui.state.ListUiState
import dcrustm.ecell.mobile.ui.state.asListUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class ComplaintInboxViewModel @Inject constructor(
    private val complaintRepository: ComplaintRepository
) : ViewModel() {

    private val refreshRequest = MutableStateFlow(0)
    val complaintState: StateFlow<ListUiState<Complaint>> = refreshRequest.flatMapLatest {
        complaintRepository.observeAllComplaints()
            .asListUiState("Could not load complaints. Please try again.")
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ListUiState())

    fun retry() { refreshRequest.value += 1 }

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    fun clearError() { _errorMessage.value = null }

    fun respond(id: String, status: ComplaintStatus, response: String) {
        viewModelScope.launch {
            runCatching { complaintRepository.updateComplaint(id, status, response) }
                .onFailure { _errorMessage.value = it.message ?: "Could not update this complaint. Please try again." }
        }
    }
}
