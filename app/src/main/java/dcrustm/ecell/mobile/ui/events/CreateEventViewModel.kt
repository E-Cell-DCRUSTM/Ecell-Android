package dcrustm.ecell.mobile.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dcrustm.ecell.mobile.domain.model.Committee
import dcrustm.ecell.mobile.domain.model.EventItem
import dcrustm.ecell.mobile.domain.model.Member
import dcrustm.ecell.mobile.domain.model.ROLE
import dcrustm.ecell.mobile.domain.repository.EventRepository
import dcrustm.ecell.mobile.session.UserSessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateEventViewModel @Inject constructor(
    private val sessionManager: UserSessionManager,
    private val eventRepository: EventRepository
) : ViewModel() {

    val currentUser: StateFlow<Member?> = sessionManager.currentUser

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun clearError() { _errorMessage.value = null }

    fun create(
        title: String,
        description: String,
        startTime: Long,
        location: String,
        isVirtual: Boolean,
        meetingLink: String,
        committee: Committee?,
        onDone: () -> Unit
    ) {
        val user = sessionManager.currentUser.value ?: return
        if (user.role != ROLE.COORDINATOR && user.role != ROLE.COMMITTEE_HEAD) return
        if (_isSaving.value) return
        _isSaving.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                eventRepository.createEvent(
                    EventItem(
                        title = title,
                        description = description,
                        startTime = startTime,
                        location = location,
                        isVirtual = isVirtual,
                        meetingLink = meetingLink,
                        committee = if (user.role == ROLE.COMMITTEE_HEAD) user.committee else committee,
                        createdByUid = user.uid,
                        createdByName = user.name
                    )
                )
                onDone()
            } catch (error: Exception) {
                _errorMessage.value = error.message ?: "Could not create the event. Please try again."
            } finally {
                _isSaving.value = false
            }
        }
    }
}
