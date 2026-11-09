package dcrustm.ecell.mobile.ui.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dcrustm.ecell.mobile.domain.model.DirectMessage
import dcrustm.ecell.mobile.domain.model.Member
import dcrustm.ecell.mobile.domain.repository.ChatRepository
import dcrustm.ecell.mobile.domain.repository.MemberRepository
import dcrustm.ecell.mobile.session.UserSessionManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val chatRepository: ChatRepository,
    memberRepository: MemberRepository,
    private val session: UserSessionManager
) : ViewModel() {
    private val conversationId: String = checkNotNull(savedStateHandle["conversationId"])
    private val otherUid: String = checkNotNull(savedStateHandle["otherUid"])

    val currentUser: StateFlow<Member?> = session.currentUser
    val otherMember: StateFlow<Member?> = memberRepository.observeMember(otherUid)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val messages: StateFlow<List<DirectMessage>> = chatRepository.observeMessages(conversationId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _error = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    fun clearError() { _error.value = null }

    fun send(text: String, onSent: () -> Unit) {
        val uid = session.currentUser.value?.uid ?: return
        viewModelScope.launch {
            runCatching { chatRepository.sendMessage(conversationId, uid, text) }
                .onSuccess { onSent() }
                .onFailure { _error.value = it.message ?: "Message could not be sent." }
        }
    }
}
