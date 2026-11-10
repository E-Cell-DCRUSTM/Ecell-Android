package dcrustm.ecell.mobile.session

import com.google.firebase.firestore.FirebaseFirestoreException
import dcrustm.ecell.mobile.domain.model.Member
import dcrustm.ecell.mobile.domain.model.MemberApplication
import dcrustm.ecell.mobile.domain.model.MembershipApplicationStatus
import dcrustm.ecell.mobile.domain.model.ROLE
import java.io.IOException
import java.net.UnknownHostException

sealed interface UserSessionState {
    data object SignedOut : UserSessionState
    data object Loading : UserSessionState
    data class ProfileIncomplete(val member: Member, val application: MemberApplication) : UserSessionState
    data class Pending(val member: Member) : UserSessionState
    data class Rejected(val member: Member, val feedback: String) : UserSessionState
    data class Approved(val member: Member) : UserSessionState
    data object ProfileUnavailable : UserSessionState
    data object NetworkFailure : UserSessionState
    data object PermissionDenied : UserSessionState
    data object RetryableFailure : UserSessionState
}

sealed interface UserSessionEvent {
    data object SignedOut : UserSessionEvent
    data object ObservationStarted : UserSessionEvent
    data object ProfileMissing : UserSessionEvent
    data class ProfileLoaded(val member: Member, val application: MemberApplication? = null) : UserSessionEvent
    data class ObservationFailed(val failure: SessionFailureCategory) : UserSessionEvent
}

enum class SessionFailureCategory { NETWORK, PERMISSION_DENIED, RETRYABLE }

fun transitionSessionState(
    current: UserSessionState,
    event: UserSessionEvent
): UserSessionState = if (
    current == UserSessionState.SignedOut &&
    event != UserSessionEvent.SignedOut &&
    event != UserSessionEvent.ObservationStarted
) current else when (event) {
    UserSessionEvent.SignedOut -> UserSessionState.SignedOut
    UserSessionEvent.ObservationStarted -> UserSessionState.Loading
    UserSessionEvent.ProfileMissing -> UserSessionState.ProfileUnavailable
    is UserSessionEvent.ProfileLoaded -> when (event.application?.status) {
        MembershipApplicationStatus.INCOMPLETE,
        MembershipApplicationStatus.NEEDS_CORRECTION -> UserSessionState.ProfileIncomplete(
            event.member,
            checkNotNull(event.application)
        )
        MembershipApplicationStatus.REJECTED -> UserSessionState.Rejected(
            event.member,
            event.application.feedback
        )
        MembershipApplicationStatus.PENDING -> if (!event.member.isApproved) {
            UserSessionState.Pending(event.member)
        } else {
            UserSessionState.RetryableFailure
        }
        MembershipApplicationStatus.APPROVED -> if (
            event.member.isApproved && event.member.role != ROLE.PENDING
        ) {
            UserSessionState.Approved(event.member)
        } else {
            UserSessionState.RetryableFailure
        }
        null -> if (event.member.isApproved && event.member.role != ROLE.PENDING) {
            UserSessionState.Approved(event.member)
        } else {
            UserSessionState.Pending(event.member)
        }
    }
    is UserSessionEvent.ObservationFailed -> when (event.failure) {
        SessionFailureCategory.NETWORK -> UserSessionState.NetworkFailure
        SessionFailureCategory.PERMISSION_DENIED -> UserSessionState.PermissionDenied
        SessionFailureCategory.RETRYABLE -> UserSessionState.RetryableFailure
    }
}

fun sessionFailureCategory(error: Throwable): SessionFailureCategory = when {
    error.causes().any { it is FirebaseFirestoreException && it.code == FirebaseFirestoreException.Code.PERMISSION_DENIED } ->
        SessionFailureCategory.PERMISSION_DENIED
    error.causes().any {
        (it is FirebaseFirestoreException && it.code in setOf(
            FirebaseFirestoreException.Code.UNAVAILABLE,
            FirebaseFirestoreException.Code.DEADLINE_EXCEEDED
        )) || it is UnknownHostException || it is IOException
    } -> SessionFailureCategory.NETWORK
    else -> SessionFailureCategory.RETRYABLE
}

private fun Throwable.causes(): Sequence<Throwable> = generateSequence(this) { it.cause }
