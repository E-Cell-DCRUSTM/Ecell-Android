package dcrustm.ecell.mobile.ui.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dcrustm.ecell.mobile.data.local.image.ImageEntity
import dcrustm.ecell.mobile.domain.dummy.ImageRepository
import dcrustm.ecell.mobile.domain.dummy.RefreshType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val repository: ImageRepository
) : ViewModel() {

    // Expose images from your Room database as a StateFlow.
    val imagesFlow: StateFlow<List<ImageEntity>> =
        repository.getImagesFlow().stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )

    private val _refreshState = MutableStateFlow<RefreshState>(RefreshState.Idle)
    val refreshState: StateFlow<RefreshState> = _refreshState

    fun refreshFull() {
        viewModelScope.launch {
            _refreshState.value = RefreshState.Loading
            repository.refreshImages(RefreshType.FULL)
                .onSuccess { _refreshState.value = RefreshState.Success }
                .onFailure { e ->
                    _refreshState.value = RefreshState.Error(e.localizedMessage ?: "Unknown error")
                }
        }
    }

}



sealed class RefreshState {

    object Idle : RefreshState()
    object Loading : RefreshState()
    object Success : RefreshState()
    data class Error(val message: String) : RefreshState()

}