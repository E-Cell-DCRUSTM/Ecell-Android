package dcrustm.ecell.mobile.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dcrustm.ecell.mobile.domain.model.Conversation
import dcrustm.ecell.mobile.domain.model.Member
import dcrustm.ecell.mobile.domain.repository.ChatRepository
import dcrustm.ecell.mobile.domain.repository.MemberRepository
import dcrustm.ecell.mobile.session.UserSessionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class MessagesViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    memberRepository: MemberRepository,
    private val session: UserSessionManager
) : ViewModel() {

    val currentUser: StateFlow<Member?> = session.currentUser
    val approvedMembers: StateFlow<List<Member>> = memberRepository.observeApprovedMembers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val conversations: StateFlow<List<Conversation>> = session.currentUser
        .flatMapLatest { member ->
            if (member?.isApproved == true) chatRepository.observeConversations(member.uid)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    fun clearError() { _error.value = null }

    fun openChat(other: Member, onOpened: (String) -> Unit) {
        val me = session.currentUser.value ?: return
        viewModelScope.launch {
            runCatching { chatRepository.openConversation(me, other) }
                .onSuccess(onOpened)
                .onFailure { _error.value = it.message ?: "Could not open this chat." }
        }
    }
}
