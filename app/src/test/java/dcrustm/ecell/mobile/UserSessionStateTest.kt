package dcrustm.ecell.mobile

import dcrustm.ecell.mobile.domain.model.Member
import dcrustm.ecell.mobile.domain.model.MemberApplication
import dcrustm.ecell.mobile.domain.model.MembershipApplicationStatus
import dcrustm.ecell.mobile.domain.model.ROLE
import dcrustm.ecell.mobile.session.UserSessionEvent
import dcrustm.ecell.mobile.session.SessionFailureCategory
import dcrustm.ecell.mobile.session.UserSessionState
import dcrustm.ecell.mobile.session.transitionSessionState
import org.junit.Assert.assertEquals
import org.junit.Test

class UserSessionStateTest {
    @Test
    fun successfulProfileReadDistinguishesPendingAndApproved() {
        val pending = Member(uid = "pending", role = ROLE.PENDING, isApproved = false)
        val approved = Member(uid = "approved", role = ROLE.MEMBER, isApproved = true)

        assertEquals(UserSessionState.Pending(pending), transitionSessionState(
            UserSessionState.Loading, UserSessionEvent.ProfileLoaded(pending)
        ))
        assertEquals(UserSessionState.Approved(approved), transitionSessionState(
            UserSessionState.Pending(pending), UserSessionEvent.ProfileLoaded(approved)
        ))
    }

    @Test
    fun applicationLifecycleSeparatesIncompletePendingRejectedAndApproved() {
        val member = Member(uid = "applicant", role = ROLE.PENDING, isApproved = false)
        assertEquals(
            UserSessionState.ProfileIncomplete(member, MemberApplication(uid = member.uid, status = MembershipApplicationStatus.INCOMPLETE)),
            transitionSessionState(UserSessionState.Loading, UserSessionEvent.ProfileLoaded(
                member, MemberApplication(uid = member.uid, status = MembershipApplicationStatus.INCOMPLETE)
            ))
        )
        assertEquals(
            UserSessionState.Pending(member),
            transitionSessionState(UserSessionState.Loading, UserSessionEvent.ProfileLoaded(
                member, MemberApplication(uid = member.uid, status = MembershipApplicationStatus.PENDING)
            ))
        )
        assertEquals(
            UserSessionState.Rejected(member, "Correct your department"),
            transitionSessionState(UserSessionState.Loading, UserSessionEvent.ProfileLoaded(
                member, MemberApplication(uid = member.uid, status = MembershipApplicationStatus.REJECTED,
                    feedback = "Correct your department")
            ))
        )
    }

    @Test
    fun missingProfileIsNotTreatedAsPending() {
        assertEquals(
            UserSessionState.ProfileUnavailable,
            transitionSessionState(UserSessionState.Loading, UserSessionEvent.ProfileMissing)
        )
    }

    @Test
    fun permissionAndNetworkFailuresRemainDistinct() {
        assertEquals(UserSessionState.PermissionDenied, transitionSessionState(
            UserSessionState.Loading, UserSessionEvent.ObservationFailed(SessionFailureCategory.PERMISSION_DENIED)
        ))
        assertEquals(UserSessionState.NetworkFailure, transitionSessionState(
            UserSessionState.Loading, UserSessionEvent.ObservationFailed(SessionFailureCategory.NETWORK)
        ))
        assertEquals(UserSessionState.RetryableFailure, transitionSessionState(
            UserSessionState.Loading, UserSessionEvent.ObservationFailed(SessionFailureCategory.RETRYABLE)
        ))
    }

    @Test
    fun retryReturnsToLoadingAndCanRecoverToApproved() {
        val member = Member(uid = "member", role = ROLE.MEMBER, isApproved = true)
        val loading = transitionSessionState(
            UserSessionState.NetworkFailure, UserSessionEvent.ObservationStarted
        )
        assertEquals(UserSessionState.Loading, loading)
        assertEquals(UserSessionState.Approved(member), transitionSessionState(
            loading, UserSessionEvent.ProfileLoaded(member)
        ))
    }

    @Test
    fun signOutWinsOverStaleProfileEvents() {
        val signedOut = transitionSessionState(
            UserSessionState.Approved(Member(uid = "member", isApproved = true)),
            UserSessionEvent.SignedOut
        )
        assertEquals(UserSessionState.SignedOut, signedOut)
        assertEquals(UserSessionState.SignedOut, transitionSessionState(
            signedOut,
            UserSessionEvent.ProfileLoaded(Member(uid = "stale", isApproved = true, role = ROLE.MEMBER))
        ))
    }
}
