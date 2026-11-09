package dcrustm.ecell.mobile.ui.directory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dcrustm.ecell.mobile.domain.model.Committee
import dcrustm.ecell.mobile.domain.model.Member
import dcrustm.ecell.mobile.domain.repository.MemberRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import dcrustm.ecell.mobile.ui.state.ListUiState
import dcrustm.ecell.mobile.ui.state.asListUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

data class DirectoryFilter(
    val query: String = "",
    val committee: Committee? = null,
    val year: Int? = null
)

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class DirectoryViewModel @Inject constructor(
    memberRepository: MemberRepository
) : ViewModel() {

    private val refreshRequest = MutableStateFlow(0)
    private val memberState = refreshRequest.flatMapLatest {
        memberRepository.observeApprovedMembers()
            .asListUiState("Could not load the member directory. Please try again.")
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ListUiState())

    private val _filter = MutableStateFlow(DirectoryFilter())

    val filteredState: StateFlow<ListUiState<Member>> = combine(memberState, _filter) { state, filter ->
        state.copy(items = state.items.filter { member ->
            val matchesQuery = filter.query.isBlank() ||
                member.name.contains(filter.query, ignoreCase = true) ||
                member.branch.contains(filter.query, ignoreCase = true)
            val matchesCommittee = filter.committee == null || member.committee == filter.committee
            val matchesYear = filter.year == null || member.year == filter.year
            matchesQuery && matchesCommittee && matchesYear
        }.sortedWith(
            compareBy<Member> { it.committee.ordinal }
                .thenByDescending { it.role.ordinal == 0 }
                .thenBy { it.name }
        ))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ListUiState())

    val filteredMembers: StateFlow<List<Member>> = filteredState.map { it.items }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun search(query: String, committee: Committee?, year: Int?) {
        _filter.value = DirectoryFilter(query, committee, year)
    }

    fun retry() { refreshRequest.value += 1 }

}
