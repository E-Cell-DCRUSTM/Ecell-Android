package dcrustm.ecell.mobile.session

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import dcrustm.ecell.mobile.data.firebase.toMember
import dcrustm.ecell.mobile.domain.model.Member
import dcrustm.ecell.mobile.domain.model.MemberContact
import dcrustm.ecell.mobile.domain.model.ROLE
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSessionManager @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    private val _currentUser = MutableStateFlow<Member?>(null)
    val currentUser: StateFlow<Member?> = _currentUser.asStateFlow()

    private val _sessionState = MutableStateFlow<UserSessionState>(
        if (firebaseAuth.currentUser == null) UserSessionState.SignedOut else UserSessionState.Loading
    )
    val sessionState: StateFlow<UserSessionState> = _sessionState.asStateFlow()

    val role: ROLE get() = _currentUser.value?.role ?: ROLE.PENDING

    val isCoordinator: Boolean get() = role == ROLE.COORDINATOR
    val isHead: Boolean get() = role == ROLE.COMMITTEE_HEAD
    val isMember: Boolean get() = role == ROLE.MEMBER || role == ROLE.COMMITTEE_HEAD || role == ROLE.COORDINATOR

    private var userDocListener: ListenerRegistration? = null
    private var contactDocListener: ListenerRegistration? = null
    private var currentContact = MemberContact()
    private var observationGeneration = 0

    init {
        firebaseAuth.addAuthStateListener { auth ->
            observationGeneration += 1
            userDocListener?.remove()
            userDocListener = null
            contactDocListener?.remove()
            contactDocListener = null
            currentContact = MemberContact()

            val uid = auth.currentUser?.uid
            if (uid == null) {
                _currentUser.value = null
                transition(UserSessionEvent.SignedOut)
            } else {
                attachListeners(uid)
            }
        }
    }

    private fun attachListeners(uid: String) {
        _currentUser.value = null
        currentContact = MemberContact()
        transition(UserSessionEvent.ObservationStarted)
        val generation = ++observationGeneration

        userDocListener = firestore.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (generation != observationGeneration) return@addSnapshotListener
                if (error != null) {
                    failSession(error)
                } else if (snapshot == null || !snapshot.exists()) {
                    _currentUser.value = null
                    transition(UserSessionEvent.ProfileMissing)
                } else {
                    runCatching { snapshot.toMember() }
                        .onSuccess { member ->
                            if (member == null) {
                                _currentUser.value = null
                                transition(UserSessionEvent.ProfileMissing)
                            } else {
                                updateMember(member)
                            }
                        }
                        .onFailure(::failSession)
                }
            }
        contactDocListener = firestore.collection("userContacts").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (generation != observationGeneration) return@addSnapshotListener
                if (error != null) {
                    failSession(error)
                } else {
                    currentContact = if (snapshot?.exists() == true) MemberContact(
                        email = snapshot.getString("email") ?: "",
                        phone = snapshot.getString("phone") ?: ""
                    ) else MemberContact()
                    _currentUser.value?.let(::updateMember)
                }
            }
    }

    private fun updateMember(member: Member) {
        val updated = member.copy(email = currentContact.email, phone = currentContact.phone)
        _currentUser.value = updated
        transition(UserSessionEvent.ProfileLoaded(updated))
    }

    private fun failSession(error: Throwable) {
        observationGeneration += 1
        userDocListener?.remove()
        userDocListener = null
        contactDocListener?.remove()
        contactDocListener = null
        _currentUser.value = null
        transition(UserSessionEvent.ObservationFailed(sessionFailureCategory(error)))
    }

    private fun transition(event: UserSessionEvent) {
        _sessionState.value = transitionSessionState(_sessionState.value, event)
    }

    /** Reattaches Firestore listeners, which also retries a failed or missing profile read. */
    fun retryProfileObservation() {
        observationGeneration += 1
        val uid = firebaseAuth.currentUser?.uid
        userDocListener?.remove()
        userDocListener = null
        contactDocListener?.remove()
        contactDocListener = null
        if (uid == null) {
            _currentUser.value = null
            transition(UserSessionEvent.SignedOut)
        } else {
            attachListeners(uid)
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
    }
}
