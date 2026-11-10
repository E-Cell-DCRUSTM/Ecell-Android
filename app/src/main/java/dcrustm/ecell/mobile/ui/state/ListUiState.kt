package dcrustm.ecell.mobile.ui.state

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/** Explicit loading, empty, and failure states for Firestore-backed lists. */
data class ListUiState<T>(
    val items: List<T> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

fun <T> Flow<List<T>>.asListUiState(fallbackMessage: String): Flow<ListUiState<T>> = flow {
    emit(ListUiState(isLoading = true))
    emitAll(map { items -> ListUiState(items = items, isLoading = false) })
}.catch {
    emit(ListUiState(isLoading = false, errorMessage = fallbackMessage))
}
