package dcrustm.ecell.mobile.ui.directory

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dcrustm.ecell.mobile.domain.model.Member
import dcrustm.ecell.mobile.domain.model.ROLE
import dcrustm.ecell.mobile.domain.repository.MemberRepository
import dcrustm.ecell.mobile.session.UserSessionManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class MemberDetailViewModel @Inject constructor(
    private val sessionManager: UserSessionManager,
    memberRepository: MemberRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val uid: String = checkNotNull(savedStateHandle["uid"])

    val member: StateFlow<Member?> = combine(
        memberRepository.observeMember(uid),
        sessionManager.currentUser
    ) { target, viewer -> target to viewer }
        .flatMapLatest { (target, viewer) ->
            if (target == null) flowOf(null)
            else if (canSeeContact(viewer, target)) {
                memberRepository.observeContact(uid).map { contact ->
                    target.copy(email = contact?.email.orEmpty(), phone = contact?.phone.orEmpty())
                }
            } else flowOf(target)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isOwnProfile: Boolean get() = sessionManager.currentUser.value?.uid == uid

    fun canSeeContact(target: Member): Boolean {
        val viewer = sessionManager.currentUser.value ?: return false
        return canSeeContact(viewer, target)
    }

    private fun canSeeContact(viewer: Member?, target: Member): Boolean {
        if (viewer == null) return false
        return when (viewer.role) {
            ROLE.COORDINATOR -> true
            ROLE.COMMITTEE_HEAD -> viewer.committee == target.committee
            ROLE.MEMBER -> viewer.uid == target.uid
            ROLE.PENDING -> false
        }
    }
}
