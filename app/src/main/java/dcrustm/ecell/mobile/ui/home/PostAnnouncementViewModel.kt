package dcrustm.ecell.mobile.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dcrustm.ecell.mobile.domain.model.Announcement
import dcrustm.ecell.mobile.domain.model.Committee
import dcrustm.ecell.mobile.domain.model.Member
import dcrustm.ecell.mobile.domain.model.ROLE
import dcrustm.ecell.mobile.domain.repository.AnnouncementRepository
import dcrustm.ecell.mobile.session.UserSessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostAnnouncementViewModel @Inject constructor(
    private val sessionManager: UserSessionManager,
    private val announcementRepository: AnnouncementRepository
) : ViewModel() {

    val currentUser: StateFlow<Member?> = sessionManager.currentUser

    private val _isPosting = MutableStateFlow(false)
    val isPosting: StateFlow<Boolean> = _isPosting.asStateFlow()
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun clearError() { _errorMessage.value = null }

    fun post(
        title: String,
        body: String,
        committee: Committee?,
        isPinned: Boolean,
        onDone: () -> Unit
    ) {
        val user = sessionManager.currentUser.value ?: return
        if (user.role != ROLE.COORDINATOR && user.role != ROLE.COMMITTEE_HEAD) return
        if (_isPosting.value) return
        _isPosting.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                announcementRepository.postAnnouncement(
                    Announcement(
                        title = title,
                        body = body,
                        authorUid = user.uid,
                        authorName = user.name,
                        committee = if (user.role == ROLE.COMMITTEE_HEAD) user.committee else committee,
                        isPinned = isPinned
                    )
                )
                onDone()
            } catch (error: Exception) {
                _errorMessage.value = error.message ?: "Could not post the announcement. Please try again."
            } finally {
                _isPosting.value = false
            }
        }
    }
}
