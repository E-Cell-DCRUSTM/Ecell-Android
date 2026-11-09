package dcrustm.ecell.mobile.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import dcrustm.ecell.mobile.data.firebase.awaitResult
import dcrustm.ecell.mobile.domain.repository.MemberRepository
import dcrustm.ecell.mobile.session.UserSessionManager
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val sessionManager: UserSessionManager,
    private val memberRepository: MemberRepository
) : ViewModel() {
    val currentUser = sessionManager.currentUser
    val sessionState = sessionManager.sessionState

    fun signOut() = sessionManager.signOut()
    fun retryProfileObservation() = sessionManager.retryProfileObservation()

    fun registerPushToken() {
        val uid = sessionManager.currentUser.value?.uid ?: return
        viewModelScope.launch {
            runCatching {
                val token = FirebaseMessaging.getInstance().token.awaitResult()
                memberRepository.registerFcmToken(uid, token)
            }
        }
    }
}
