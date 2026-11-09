package dcrustm.ecell.mobile

import dcrustm.ecell.mobile.domain.model.ROLE
import dcrustm.ecell.mobile.navigation.Screen
import dcrustm.ecell.mobile.navigation.screensForRole
import org.junit.Assert.assertEquals
import org.junit.Test

class ScreenVisibilityTest {

    @Test
    fun pendingMembersCannotOpenTheApprovedMemberWorkspace() {
        assertEquals(emptySet<String>(), screensForRole(ROLE.PENDING).map(Screen::route).toSet())
    }

    @Test
    fun ordinaryMembersSeeMemberFeaturesButNoCoordinatorOrHeadTools() {
        assertEquals(
            setOf("home", "events", "directory", "complaints", "polls", "messages"),
            screensForRole(ROLE.MEMBER).map(Screen::route).toSet(),
        )
    }

    @Test
    fun committeeHeadsSeeTheirCommitteeToolsWithoutCoordinatorInbox() {
        assertEquals(
            setOf("home", "events", "directory", "my_committee", "complaints", "polls", "messages"),
            screensForRole(ROLE.COMMITTEE_HEAD).map(Screen::route).toSet(),
        )
    }

    @Test
    fun coordinatorsSeeApprovalAndComplaintManagementRoutes() {
        assertEquals(
            setOf("home", "events", "directory", "members", "inbox", "polls", "messages"),
            screensForRole(ROLE.COORDINATOR).map(Screen::route).toSet(),
        )
    }
}
