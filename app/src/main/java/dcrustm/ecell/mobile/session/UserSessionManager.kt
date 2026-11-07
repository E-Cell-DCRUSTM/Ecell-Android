package dcrustm.ecell.mobile.session

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import dcrustm.ecell.mobile.data.firebase.toMember
import dcrustm.ecell.mobile.domain.model.Member
import dcrustm.ecell.mobile.domain.model.MemberContact
import dcrustm.ecell.mobile.domain.model.ROLE
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSessionManager @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    private val _currentUser = MutableStateFlow<Member?>(null)
    val currentUser: StateFlow<Member?> = _currentUser.asStateFlow()

    private val _isSignedIn = MutableStateFlow(firebaseAuth.currentUser != null)
    val isSignedIn: StateFlow<Boolean> = _isSignedIn.asStateFlow()

    val role: ROLE get() = _currentUser.value?.role ?: ROLE.PENDING

    val isCoordinator: Boolean get() = role == ROLE.COORDINATOR
    val isHead: Boolean get() = role == ROLE.COMMITTEE_HEAD
    val isMember: Boolean get() = role == ROLE.MEMBER || role == ROLE.COMMITTEE_HEAD || role == ROLE.COORDINATOR

    private var userDocListener: ListenerRegistration? = null
    private var contactDocListener: ListenerRegistration? = null
    private var currentContact = MemberContact()

    init {
        firebaseAuth.addAuthStateListener { auth ->
            userDocListener?.remove()
            userDocListener = null
            contactDocListener?.remove()
            contactDocListener = null
            currentContact = MemberContact()

            val uid = auth.currentUser?.uid
            _isSignedIn.value = uid != null
            if (uid == null) {
                _currentUser.value = null
            } else {
                userDocListener = firestore.collection("users").document(uid)
                    .addSnapshotListener { snapshot, _ ->
                        _currentUser.value = snapshot?.toMember()?.copy(
                            email = currentContact.email,
                            phone = currentContact.phone
                        )
                    }
                contactDocListener = firestore.collection("userContacts").document(uid)
                    .addSnapshotListener { snapshot, _ ->
                        currentContact = if (snapshot?.exists() == true) MemberContact(
                            email = snapshot.getString("email") ?: "",
                            phone = snapshot.getString("phone") ?: ""
                        ) else MemberContact()
                        _currentUser.value = _currentUser.value?.copy(
                            email = currentContact.email,
                            phone = currentContact.phone
                        )
                    }
            }
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
    }
}
