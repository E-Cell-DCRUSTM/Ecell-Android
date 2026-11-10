package dcrustm.ecell.mobile

import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import dcrustm.ecell.mobile.domain.model.Member
import dcrustm.ecell.mobile.domain.model.MemberApplication
import dcrustm.ecell.mobile.domain.model.MembershipApplicationStatus
import dcrustm.ecell.mobile.domain.model.PendingMemberApplication
import dcrustm.ecell.mobile.domain.model.Committee
import dcrustm.ecell.mobile.domain.model.ROLE
import dcrustm.ecell.mobile.navigation.SessionGateContent
import dcrustm.ecell.mobile.session.UserSessionState
import dcrustm.ecell.mobile.ui.coordinator.PendingMemberCard
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SessionGateScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun loadingAndPendingStatesHaveDifferentScreens() {
        val state = mutableStateOf<UserSessionState>(UserSessionState.Loading)
        composeRule.setContent {
            SessionGateContent(state.value, {}, {}, approvedContent = {
                androidx.compose.material3.Text("Workspace")
            })
        }
        composeRule.onNodeWithText("Checking your account").assertIsDisplayed()

        composeRule.runOnIdle {
            state.value = UserSessionState.Pending(Member(uid = "pending"))
        }
        composeRule.onNodeWithText("Waiting for approval").assertIsDisplayed()
    }

    @Test
    fun approvedStateOpensWorkspace() {
        composeRule.setContent {
            SessionGateContent(
                UserSessionState.Approved(Member(uid = "member", role = ROLE.MEMBER, isApproved = true)),
                {}, {}, approvedContent = { androidx.compose.material3.Text("Workspace") }
            )
        }
        composeRule.onNodeWithText("Workspace").assertIsDisplayed()
    }

    @Test
    fun profileUnavailablePermissionAndNetworkErrorsAreShown() {
        val state = mutableStateOf<UserSessionState>(UserSessionState.ProfileUnavailable)
        composeRule.setContent {
            SessionGateContent(state.value, {}, {}, approvedContent = {
                androidx.compose.material3.Text("Workspace")
            })
        }
        val cases = listOf(
            UserSessionState.ProfileUnavailable to "Profile unavailable",
            UserSessionState.PermissionDenied to "Access denied",
            UserSessionState.NetworkFailure to "Connection problem"
        )
        cases.forEach { (nextState, expectedTitle) ->
            // Drive the screen through each failure state in the same composition.
            composeRule.runOnIdle { state.value = nextState }
            composeRule.onNodeWithText(expectedTitle).assertIsDisplayed()
        }
    }

    @Test
    fun retryCanRecoverAndErrorScreenCanSignOut() {
        val state = mutableStateOf<UserSessionState>(UserSessionState.NetworkFailure)
        var retries = 0
        var signOuts = 0
        composeRule.setContent {
            SessionGateContent(
                state.value,
                onRetry = {
                    retries++
                    state.value = UserSessionState.Approved(
                        Member(uid = "member", role = ROLE.MEMBER, isApproved = true)
                    )
                },
                onSignOut = { signOuts++ },
                approvedContent = { androidx.compose.material3.Text("Workspace") }
            )
        }
        composeRule.onNodeWithText("Retry").performClick()
        composeRule.onNodeWithText("Workspace").assertIsDisplayed()
        composeRule.runOnIdle { assertEquals(1, retries) }

        state.value = UserSessionState.PermissionDenied
        composeRule.onNodeWithText("Sign out").performClick()
        composeRule.runOnIdle { assertEquals(1, signOuts) }
    }

    @Test
    fun signedOutStateIsExplicit() {
        composeRule.setContent {
            SessionGateContent(UserSessionState.SignedOut, {}, {}, approvedContent = {
                androidx.compose.material3.Text("Workspace")
            })
        }
        composeRule.onNodeWithText("Signed out").assertIsDisplayed()
    }

    @Test
    fun incompleteProfileOpensSetupAndRejectedApplicationCanReapply() {
        val member = Member(uid = "applicant")
        val state = mutableStateOf<UserSessionState>(UserSessionState.ProfileIncomplete(
            member,
            MemberApplication(uid = member.uid, status = MembershipApplicationStatus.INCOMPLETE)
        ))
        var reapplyCalls = 0
        composeRule.setContent {
            SessionGateContent(
                state = state.value,
                onRetry = {},
                onSignOut = {},
                onReapply = { reapplyCalls++ },
                profileSetupContent = { androidx.compose.material3.Text("Profile setup form") },
                approvedContent = {}
            )
        }
        composeRule.onNodeWithText("Profile setup form").assertIsDisplayed()
        composeRule.runOnIdle {
            state.value = UserSessionState.Rejected(member, "Please correct your branch.")
        }
        composeRule.onNodeWithText("Application declined").assertIsDisplayed()
        composeRule.onNodeWithText("Review and reapply").performClick()
        composeRule.runOnIdle { assertEquals(1, reapplyCalls) }
    }

    @Test
    fun coordinatorMustSelectCommitteeAndConfirmBeforeActivatingMember() {
        val application = PendingMemberApplication(
            member = Member(uid = "applicant", name = "Aarav Sharma", branch = "Computer Engineering"),
            application = MemberApplication(uid = "applicant", status = MembershipApplicationStatus.PENDING)
        )
        var approved = false
        var assignedCommittee: Committee? = null
        composeRule.setContent {
            MaterialTheme {
                PendingMemberCard(
                    application = application,
                    photo = null,
                    onApprove = { committee, _ -> approved = true; assignedCommittee = committee },
                    onReject = {},
                    onCorrection = {}
                )
            }
        }
        composeRule.onNodeWithText("Approve application").assertIsDisplayed()
        composeRule.runOnIdle { assertEquals(false, approved) }
        composeRule.onNode(hasText("—") and hasClickAction()).performClick()
        composeRule.onNodeWithText(Committee.EVENTS.displayName).performClick()
        composeRule.onNodeWithText("Approve application").performClick()
        composeRule.onNodeWithText("Approve E-Cell membership?").assertIsDisplayed()
        composeRule.runOnIdle { assertEquals(false, approved) }
        composeRule.onNodeWithText("Approve member").performClick()
        composeRule.runOnIdle {
            assertEquals(true, approved)
            assertEquals(Committee.EVENTS, assignedCommittee)
        }
    }
}
