package dcrustm.ecell.mobile.ui.onboarding.profile

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dcrustm.ecell.mobile.BuildConfig
import dcrustm.ecell.mobile.domain.model.Committee
import dcrustm.ecell.mobile.domain.model.MemberApplication
import dcrustm.ecell.mobile.domain.model.MembershipApplicationStatus
import dcrustm.ecell.mobile.domain.model.ProfileDraft
import dcrustm.ecell.mobile.domain.repository.MemberRepository
import dcrustm.ecell.mobile.domain.repository.ProfilePhotoRepository
import dcrustm.ecell.mobile.domain.validation.ProfileDraftValidator
import dcrustm.ecell.mobile.domain.validation.ProfileFieldRequirements
import dcrustm.ecell.mobile.session.UserSessionManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

data class ProfileSetupUiState(
    val isLoading: Boolean = true,
    val draft: ProfileDraft = ProfileDraft(),
    val currentStep: Int = 1,
    val isSaving: Boolean = false,
    val isUploadingPhoto: Boolean = false,
    val isSubmitting: Boolean = false,
    val photoPreview: Bitmap? = null,
    val fieldErrors: Map<String, String> = emptyMap(),
    val errorMessage: String? = null,
    val saveMessage: String? = null,
    val coordinatorFeedback: String = ""
)

@HiltViewModel
class ProfileSetupViewModel @Inject constructor(
    private val sessionManager: UserSessionManager,
    private val memberRepository: MemberRepository,
    private val photoRepository: ProfilePhotoRepository
) : ViewModel() {

    private val profileRequirements = ProfileFieldRequirements(
        phoneRequired = BuildConfig.PROFILE_PHONE_REQUIRED,
        enrollmentNumberRequired = BuildConfig.PROFILE_ENROLLMENT_REQUIRED
    )

    private val uid = checkNotNull(sessionManager.currentUser.value?.uid) {
        "Your session is still loading. Retry sign-in to continue."
    }
    private val _state = MutableStateFlow(ProfileSetupUiState())
    val state: StateFlow<ProfileSetupUiState> = _state.asStateFlow()

    private val saveMutex = Mutex()
    private var autosaveJob: Job? = null
    private var initialized = false

    init {
        viewModelScope.launch {
            combine(
                memberRepository.observeMember(uid),
                memberRepository.observeContact(uid),
                memberRepository.observeApplication(uid)
            ) { member, contact, application -> Triple(member, contact, application) }
                .collect { (member, contact, application) ->
                    if (member == null) return@collect
                    if (!initialized) {
                        initialized = true
                        val draft = ProfileDraft(
                            name = member.name,
                            email = contact?.email ?: member.email,
                            phone = contact?.phone ?: member.phone,
                            year = member.year.coerceIn(1, 4),
                            branch = member.branch,
                            section = application?.section.orEmpty(),
                            enrollmentNumber = application?.enrollmentNumber.orEmpty(),
                            preferredCommittee = application?.preferredCommittee ?: Committee.NONE,
                            bio = member.bio,
                            profilePhotoPath = member.profilePhotoUrl.takeIf {
                                it == "memberPhotos/$uid/profile.jpg"
                            }.orEmpty(),
                            activeStep = application?.activeStep?.coerceIn(1, 5) ?: 1
                        )
                        _state.update {
                            it.copy(
                                isLoading = false,
                                draft = draft,
                                currentStep = draft.activeStep,
                                coordinatorFeedback = application?.feedback.orEmpty()
                            )
                        }
                        if (draft.profilePhotoPath.isNotBlank()) {
                            viewModelScope.launch {
                                val bitmap = photoRepository.loadProfilePhoto(uid, draft.profilePhotoPath)
                                _state.update { it.copy(photoPreview = bitmap) }
                            }
                        }
                    } else if (application?.status == MembershipApplicationStatus.NEEDS_CORRECTION) {
                        _state.update { it.copy(coordinatorFeedback = application.feedback) }
                    }
                }
        }
    }

    fun updateName(value: String) = updateDraft { it.copy(name = value.take(100)) }
    fun updatePhone(value: String) = updateDraft { it.copy(phone = value.take(32)) }
    fun updateYear(value: Int) = updateDraft { it.copy(year = value) }
    fun updateBranch(value: String) = updateDraft { it.copy(branch = value.take(100)) }
    fun updateSection(value: String) = updateDraft { it.copy(section = value.take(40)) }
    fun updateEnrollmentNumber(value: String) = updateDraft { it.copy(enrollmentNumber = value.take(40)) }
    fun updatePreferredCommittee(value: Committee) = updateDraft { it.copy(preferredCommittee = value) }
    fun updateBio(value: String) = updateDraft { it.copy(bio = value.take(1000)) }

    private fun updateDraft(transform: (ProfileDraft) -> ProfileDraft) {
        _state.update { current ->
            current.copy(
                draft = transform(current.draft),
                fieldErrors = emptyMap(),
                errorMessage = null,
                saveMessage = null
            )
        }
        scheduleAutosave()
    }

    fun onPhotoSelected(uri: Uri) {
        if (_state.value.isUploadingPhoto) return
        _state.update { it.copy(isUploadingPhoto = true, errorMessage = null, saveMessage = null) }
        viewModelScope.launch {
            runCatching { photoRepository.uploadOwnProfilePhoto(uid, uri) }
                .onSuccess { upload ->
                    val newDraft = _state.value.draft.copy(profilePhotoPath = upload.storagePath)
                    _state.update { it.copy(draft = newDraft, photoPreview = upload.preview, isUploadingPhoto = false) }
                    persistDraft(newDraft, showSavedMessage = true)
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isUploadingPhoto = false,
                            errorMessage = error.message ?: "Could not upload this photo. You can continue without it."
                        )
                    }
                }
        }
    }

    fun saveProgress() {
        autosaveJob?.cancel()
        viewModelScope.launch { persistDraft(_state.value.draft, showSavedMessage = true) }
    }

    fun continueOrSubmit() {
        if (_state.value.isSaving || _state.value.isSubmitting || _state.value.isUploadingPhoto) return
        val current = _state.value
        val errors = ProfileDraftValidator.validateStep(
            current.currentStep, current.draft, uid, profileRequirements
        )
        if (errors.isNotEmpty()) {
            _state.update { it.copy(fieldErrors = errors, errorMessage = null) }
            return
        }

        if (current.currentStep < 5) {
            val nextStep = current.currentStep + 1
            val nextDraft = current.draft.copy(activeStep = nextStep)
            autosaveJob?.cancel()
            viewModelScope.launch {
                if (persistDraft(nextDraft, showSavedMessage = false)) {
                    _state.update { it.copy(draft = nextDraft, currentStep = nextStep, fieldErrors = emptyMap()) }
                }
            }
            return
        }

        val submitErrors = ProfileDraftValidator.validate(current.draft, uid, profileRequirements)
        if (submitErrors.isNotEmpty()) {
            _state.update { it.copy(fieldErrors = submitErrors, errorMessage = "Check the highlighted fields.") }
            return
        }
        autosaveJob?.cancel()
        _state.update { it.copy(isSubmitting = true, errorMessage = null, fieldErrors = emptyMap()) }
        viewModelScope.launch {
            runCatching { saveMutex.withLock { memberRepository.submitOnboardingProfile(uid, current.draft) } }
                .onSuccess {
                    _state.update { it.copy(isSubmitting = false, saveMessage = "Application submitted") }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = error.message ?: "Could not submit your profile. Please retry."
                        )
                    }
                }
        }
    }

    fun goToStep(step: Int) {
        if (step !in 1..5 || step == _state.value.currentStep || _state.value.isSaving) return
        val nextDraft = _state.value.draft.copy(activeStep = step)
        autosaveJob?.cancel()
        viewModelScope.launch {
            if (persistDraft(nextDraft, showSavedMessage = false)) {
                _state.update { it.copy(draft = nextDraft, currentStep = step, fieldErrors = emptyMap()) }
            }
        }
    }

    fun back() = goToStep((_state.value.currentStep - 1).coerceAtLeast(1))

    fun signOut() = sessionManager.signOut()

    private fun scheduleAutosave() {
        autosaveJob?.cancel()
        autosaveJob = viewModelScope.launch {
            delay(AUTOSAVE_DELAY_MS)
            persistDraft(_state.value.draft, showSavedMessage = false)
        }
    }

    private suspend fun persistDraft(draft: ProfileDraft, showSavedMessage: Boolean): Boolean =
        saveMutex.withLock {
            _state.update { it.copy(isSaving = true, errorMessage = null) }
            val result = runCatching { memberRepository.saveOnboardingDraft(uid, draft) }
            _state.update {
                it.copy(
                    isSaving = false,
                    errorMessage = result.exceptionOrNull()?.message,
                    saveMessage = if (result.isSuccess && showSavedMessage) "Progress saved" else it.saveMessage
                )
            }
            result.isSuccess
        }

    private companion object {
        const val AUTOSAVE_DELAY_MS = 700L
    }
}
