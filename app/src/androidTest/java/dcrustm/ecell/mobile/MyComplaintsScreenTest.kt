package dcrustm.ecell.mobile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import dcrustm.ecell.mobile.domain.model.Complaint
import dcrustm.ecell.mobile.domain.model.ComplaintStatus
import dcrustm.ecell.mobile.ui.complaint.MyComplaintsContent
import dcrustm.ecell.mobile.ui.state.ListUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MyComplaintsScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun ownComplaintShowsStatusAndCoordinatorResponseAndBackWorks() {
        var backCount = 0
        val complaint = Complaint(
            id = "mine",
            title = "Broken projector",
            body = "The projector is broken in the seminar room.",
            status = ComplaintStatus.IN_REVIEW,
            response = "A replacement has been ordered."
        )

        composeRule.setContent {
            MyComplaintsContent(
                state = ListUiState(items = listOf(complaint), isLoading = false),
                onBack = { backCount++ },
                onRetry = {},
                onFileComplaint = {}
            )
        }

        composeRule.onNodeWithText("In Review").assertIsDisplayed()
        composeRule.onNodeWithText("Coordinator response").assertIsDisplayed()
        composeRule.onNodeWithText("A replacement has been ordered.").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Back").performClick()
        composeRule.runOnIdle { assertEquals(1, backCount) }
    }

    @Test
    fun loadFailureCanBeRetried() {
        var retryCount = 0
        composeRule.setContent {
            MyComplaintsContent(
                state = ListUiState(
                    isLoading = false,
                    errorMessage = "Could not load your complaints. Please try again."
                ),
                onBack = {},
                onRetry = { retryCount++ },
                onFileComplaint = {}
            )
        }

        composeRule.onNodeWithText("Could not load your complaints. Please try again.").assertIsDisplayed()
        composeRule.onNodeWithText("Retry").performClick()
        composeRule.runOnIdle { assertEquals(1, retryCount) }
    }

    @Test
    fun emptyStateCanNavigateToComplaintForm() {
        var fileCount = 0
        composeRule.setContent {
            MyComplaintsContent(
                state = ListUiState(isLoading = false),
                onBack = {},
                onRetry = {},
                onFileComplaint = { fileCount++ }
            )
        }

        composeRule.onNodeWithText("No complaints yet").assertIsDisplayed()
        composeRule.onNodeWithText("File a complaint").performClick()
        composeRule.runOnIdle { assertEquals(1, fileCount) }
    }
}
