package dcrustm.ecell.mobile

import dcrustm.ecell.mobile.ui.state.ListUiState
import dcrustm.ecell.mobile.ui.state.asListUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class ListUiStateTest {
    @Test
    fun listFlowEmitsLoadingThenLoadedData() = runBlocking {
        val states = flowOf(listOf("event")).asListUiState("Could not load events.").toList()

        assertEquals(ListUiState<String>(isLoading = true), states.first())
        assertEquals(ListUiState(items = listOf("event"), isLoading = false), states.last())
    }

    @Test
    fun listFlowTurnsListenerFailureIntoRetryableErrorState() = runBlocking {
        val broken: Flow<List<String>> = flow { throw IllegalStateException("SDK detail") }
        val states = broken.asListUiState("Could not load events.").toList()

        assertEquals(
            ListUiState<String>(isLoading = false, errorMessage = "Could not load events."),
            states.last()
        )
    }
}
