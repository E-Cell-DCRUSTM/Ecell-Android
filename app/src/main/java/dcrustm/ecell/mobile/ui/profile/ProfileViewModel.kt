package dcrustm.ecell.mobile.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.graphics.Bitmap
import android.net.Uri
import dagger.hilt.android.lifecycle.HiltViewModel
import dcrustm.ecell.mobile.domain.model.Member
import dcrustm.ecell.mobile.domain.repository.MemberRepository
import dcrustm.ecell.mobile.domain.repository.ProfilePhotoRepository
import dcrustm.ecell.mobile.session.UserSessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val sessionManager: UserSessionManager,
    private val memberRepository: MemberRepository,
    private val photoRepository: ProfilePhotoRepository
) : ViewModel() {

    val currentUser: StateFlow<Member?> = sessionManager.currentUser

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    private val _photoPreview = MutableStateFlow<Bitmap?>(null)
    val photoPreview: StateFlow<Bitmap?> = _photoPreview.asStateFlow()
    private val _isUploadingPhoto = MutableStateFlow(false)
    val isUploadingPhoto: StateFlow<Boolean> = _isUploadingPhoto.asStateFlow()
    fun clearError() { _errorMessage.value = null }

    init {
        viewModelScope.launch {
            sessionManager.currentUser.collect { member ->
                val path = member?.profilePhotoUrl.orEmpty()
                if (member != null && path.isNotBlank()) {
                    _photoPreview.value = photoRepository.loadProfilePhoto(member.uid, path)
                } else if (path.isBlank()) {
                    _photoPreview.value = null
                }
            }
        }
    }

    fun updateProfile(name: String, phone: String, bio: String) {
        val uid = sessionManager.currentUser.value?.uid ?: return
        if (_isSaving.value) return
        if (name.trim().length !in 2..100) {
            _errorMessage.value = "Enter your name (2–100 characters)."
            return
        }
        if (phone.length > 32 || bio.length > 1000) {
            _errorMessage.value = "Some profile details are longer than allowed."
            return
        }
        _isSaving.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val member = sessionManager.currentUser.value ?: return@launch
                memberRepository.updateOwnProfile(uid, name, phone, bio, member.profilePhotoUrl)
            } catch (error: Exception) {
                _errorMessage.value = error.message ?: "Could not save your profile. Please try again."
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun selectPhoto(uri: Uri) {
        val member = sessionManager.currentUser.value?.takeIf { it.isApproved } ?: return
        if (_isUploadingPhoto.value) return
        _isUploadingPhoto.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            runCatching { photoRepository.uploadOwnProfilePhoto(member.uid, uri) }
                .onSuccess { uploaded ->
                    runCatching {
                        memberRepository.updateOwnProfile(
                            member.uid, member.name, member.phone, member.bio, uploaded.storagePath
                        )
                    }.onSuccess {
                        _photoPreview.value = uploaded.preview
                    }.onFailure { _errorMessage.value = it.message ?: "Photo uploaded but could not be saved to your profile." }
                }
                .onFailure { _errorMessage.value = it.message ?: "Could not upload this photo." }
            _isUploadingPhoto.value = false
        }
    }

    fun signOut() = sessionManager.signOut()
}
