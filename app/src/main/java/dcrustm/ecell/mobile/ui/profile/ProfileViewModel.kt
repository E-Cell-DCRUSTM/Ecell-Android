package dcrustm.ecell.mobile.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dcrustm.ecell.mobile.data.local.profile.ProfileEntity
import dcrustm.ecell.mobile.domain.dummy.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val profile = repository.getLocalProfile()
            _uiState.value = ProfileUiState(profile = profile, isLoading = false)
        }
    }
}

data class ProfileUiState(
    val profile: ProfileEntity? = null,
    val isLoading: Boolean = true
)