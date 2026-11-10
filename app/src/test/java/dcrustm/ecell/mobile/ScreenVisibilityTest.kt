package dcrustm.ecell.mobile

import dcrustm.ecell.mobile.domain.model.ROLE
import dcrustm.ecell.mobile.navigation.Screen
import dcrustm.ecell.mobile.navigation.moreScreensForRole
import dcrustm.ecell.mobile.navigation.screensForRole
import org.junit.Assert.assertEquals
import org.junit.Test

class ScreenVisibilityTest {

    @Test
    fun pendingMembersCannotOpenTheApprovedMemberWorkspace() {
        assertEquals(emptySet<String>(), screensForRole(ROLE.PENDING).map(Screen::route).toSet())
        assertEquals(emptySet<String>(), moreScreensForRole(ROLE.PENDING).map(Screen::route).toSet())
    }

    @Test
    fun everyApprovedRoleHasFiveLabeledPrimaryDestinations() {
        val expected = setOf("home", "events", "polls", "messages", "more")
        assertEquals(expected, screensForRole(ROLE.MEMBER).map(Screen::route).toSet())
        assertEquals(expected, screensForRole(ROLE.COMMITTEE_HEAD).map(Screen::route).toSet())
        assertEquals(expected, screensForRole(ROLE.COORDINATOR).map(Screen::route).toSet())
        assertEquals(5, screensForRole(ROLE.COORDINATOR).size)
    }

    @Test
    fun ordinaryMembersSeeOnlyMemberToolsUnderMore() {
        assertEquals(
            setOf("directory", "complaints", "my_complaints"),
            moreScreensForRole(ROLE.MEMBER).map(Screen::route).toSet(),
        )
    }

    @Test
    fun committeeHeadsSeeCommitteeToolsWithoutCoordinatorInbox() {
        assertEquals(
            setOf("directory", "my_committee", "complaints", "my_complaints"),
            moreScreensForRole(ROLE.COMMITTEE_HEAD).map(Screen::route).toSet(),
        )
    }

    @Test
    fun coordinatorsSeeApprovalAndComplaintManagementRoutes() {
        assertEquals(
            setOf("directory", "members", "inbox"),
            moreScreensForRole(ROLE.COORDINATOR).map(Screen::route).toSet(),
        )
    }
}
