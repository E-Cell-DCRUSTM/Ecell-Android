package dcrustm.ecell.mobile.ui.onboarding.profile

import android.net.Uri

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dcrustm.ecell.mobile.domain.model.Committee
import dcrustm.ecell.mobile.domain.model.ProfileDraft
import dcrustm.ecell.mobile.ui.profile.createProfileCaptureUri

@Composable
fun ProfileSetupScreen(viewModel: ProfileSetupViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let(viewModel::onPhotoSelected) }
    val context = LocalContext.current
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { captured ->
        val uri = cameraUri
        cameraUri = null
        if (captured && uri != null) viewModel.onPhotoSelected(uri)
    }

    Scaffold { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                if (state.currentStep > 1) {
                    IconButton(onClick = viewModel::back) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Previous step")
                    }
                } else {
                    Spacer(Modifier.size(48.dp))
                }
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("E-CELL DCRUSTM", style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text("Member profile", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold)
                }
                TextButton(onClick = viewModel::signOut) { Text("Sign out") }
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Step ${state.currentStep} of 5", style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary)
                        Text("${state.currentStep * 20}%", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    LinearProgressIndicator(
                        progress = { state.currentStep / 5f },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape)
                    )
                    Text(stepTitle(state.currentStep), style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold)
                    Text(stepSubtitle(state.currentStep), style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            state.coordinatorFeedback.takeIf(String::isNotBlank)?.let { feedback ->
                Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.tertiaryContainer) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Coordinator feedback", style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold)
                        Text(feedback, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            when (state.currentStep) {
                1 -> PersonalInformation(state.draft, state.fieldErrors, viewModel)
                2 -> AcademicInformation(state.draft, state.fieldErrors, viewModel)
                3 -> PhotoStep(
                    state,
                    onChoosePhoto = {
                        photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    onTakePhoto = {
                        runCatching { createProfileCaptureUri(context) }
                            .onSuccess { uri -> cameraUri = uri; camera.launch(uri) }
                    }
                )
                4 -> ECellInformation(state.draft, state.fieldErrors, viewModel)
                5 -> ReviewInformation(state.draft, state.photoPreview?.asImageBitmap())
            }

            state.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
            state.saveMessage?.let {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Outlined.CheckCircle, contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = viewModel::saveProgress,
                    enabled = !state.isSaving && !state.isUploadingPhoto && !state.isSubmitting,
                    modifier = Modifier.weight(1f)
                ) { Text(if (state.isSaving) "Saving…" else "Save progress") }
                Button(
                    onClick = viewModel::continueOrSubmit,
                    enabled = !state.isSaving && !state.isUploadingPhoto && !state.isSubmitting,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(when {
                        state.isUploadingPhoto -> "Uploading…"
                        state.isSubmitting -> "Submitting…"
                        state.currentStep == 5 -> "Submit application"
                        else -> "Continue"
                    })
                }
            }
            Text(
                "Your enrollment number and contact details are shared only with authorized E-Cell coordinators.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PersonalInformation(draft: ProfileDraft, errors: Map<String, String>, vm: ProfileSetupViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Field("Full name", draft.name, vm::updateName, errors["name"], "Your name as it should appear on your member ID")
        OutlinedTextField(
            value = draft.email, onValueChange = {}, readOnly = true, enabled = false,
            modifier = Modifier.fillMaxWidth(), label = { Text("University email") },
            supportingText = { Text("From your signed-in account") }, singleLine = true
        )
        Field("Contact number (optional)", draft.phone, vm::updatePhone, errors["phone"], "Used only for E-Cell coordination",
            keyboardType = KeyboardType.Phone)
    }
}

@Composable
private fun AcademicInformation(draft: ProfileDraft, errors: Map<String, String>, vm: ProfileSetupViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(value = "Deenbandhu Chhotu Ram University of Science and Technology",
            onValueChange = {}, readOnly = true, enabled = false, modifier = Modifier.fillMaxWidth(),
            label = { Text("University") })
        Field("Department / branch", draft.branch, vm::updateBranch, errors["branch"], "For example, Computer Engineering")
        SelectionField("Current academic year", "${draft.year}${ordinal(draft.year)} year",
            listOf("1st year", "2nd year", "3rd year", "4th year"), errors["year"]) { choice ->
            vm.updateYear(choice.substringBefore(' ').toInt())
        }
        Field("Section (optional)", draft.section, vm::updateSection, errors["section"])
        Field("Enrollment / roll number (optional)", draft.enrollmentNumber, vm::updateEnrollmentNumber,
            errors["enrollmentNumber"], "Visible to coordinators reviewing your application")
    }
}

@Composable
private fun PhotoStep(state: ProfileSetupUiState, onChoosePhoto: () -> Unit, onTakePhoto: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.size(168.dp)) {
            val preview = state.photoPreview
            if (preview != null) {
                Image(preview.asImageBitmap(), contentDescription = "Profile photo preview",
                    modifier = Modifier.fillMaxSize().clip(CircleShape))
            } else {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.AddAPhoto, contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(54.dp))
                }
            }
        }
        Text(if (state.photoPreview == null) "Add a profile photo" else "Photo preview",
            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text("We crop to a square, resize to 512 × 512 and compress before upload. Your photo is private until your membership is approved.",
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onChoosePhoto, enabled = !state.isUploadingPhoto) {
                Text(if (state.photoPreview == null) "Choose photo" else "Replace photo")
            }
            OutlinedButton(onClick = onTakePhoto, enabled = !state.isUploadingPhoto) {
                Icon(Icons.Outlined.PhotoCamera, contentDescription = null)
                Spacer(Modifier.size(6.dp))
                Text("Take photo")
            }
        }
        if (state.isUploadingPhoto) CircularProgressIndicator()
    }
}

@Composable
private fun ECellInformation(draft: ProfileDraft, errors: Map<String, String>, vm: ProfileSetupViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SelectionField("Preferred committee (optional)",
            if (draft.preferredCommittee == Committee.NONE) "No preference" else draft.preferredCommittee.displayName,
            Committee.entries.filter { it != Committee.NONE }.map { it.displayName } + "No preference",
            errors["preferredCommittee"]) { choice ->
            vm.updatePreferredCommittee(Committee.entries.firstOrNull { it.displayName == choice } ?: Committee.NONE)
        }
        // Map the selected label explicitly so preference never becomes an assigned role or committee.
        if (draft.preferredCommittee == Committee.NONE) {
            Text("Your preference helps coordinators review your application. It does not assign you to a committee.",
                color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
        OutlinedTextField(
            value = draft.bio, onValueChange = vm::updateBio, modifier = Modifier.fillMaxWidth().heightIn(min = 150.dp),
            label = { Text("A short introduction (optional)") }, supportingText = { Text("${draft.bio.length}/1000") },
            isError = errors.containsKey("bio")
        )
        errors["bio"]?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
    }
}

@Composable
private fun ReviewInformation(draft: ProfileDraft, photo: androidx.compose.ui.graphics.ImageBitmap?) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surfaceContainerLow) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                if (photo != null) Image(photo, "Profile photo", Modifier.size(76.dp).clip(CircleShape))
                ReviewRow("Name", draft.name)
                ReviewRow("Email", draft.email)
                ReviewRow("Phone", draft.phone.ifBlank { "Not provided" })
                HorizontalDivider()
                ReviewRow("University", "DCRUST")
                ReviewRow("Department", draft.branch)
                ReviewRow("Year", "${draft.year}${ordinal(draft.year)} year")
                ReviewRow("Section", draft.section.ifBlank { "Not provided" })
                ReviewRow("Enrollment number", draft.enrollmentNumber.ifBlank { "Not provided" })
                HorizontalDivider()
                ReviewRow("Committee preference", draft.preferredCommittee.displayName)
                ReviewRow("Introduction", draft.bio.ifBlank { "Not provided" })
            }
        }
        Text("A coordinator will review these details before your membership is activated.",
            color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun Field(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    error: String?,
    hint: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange, modifier = Modifier.fillMaxWidth(),
        label = { Text(label) }, supportingText = hint?.let { { Text(it) } },
        singleLine = true, isError = error != null,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, capitalization = KeyboardCapitalization.Words)
    )
    error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
}

@Composable
private fun SelectionField(label: String, selected: String, options: List<String>, error: String?, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth()) {
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(selected, style = MaterialTheme.typography.bodyLarge)
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option -> DropdownMenuItem(text = { Text(option) }, onClick = {
                expanded = false
                onSelected(option)
            }) }
        }
    }
    error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
}

@Composable
private fun ReviewRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value.ifBlank { "—" }, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun stepTitle(step: Int) = when (step) {
    1 -> "Personal information"
    2 -> "Academic information"
    3 -> "Profile photograph"
    4 -> "Your E-Cell interests"
    else -> "Review and submit"
}

private fun stepSubtitle(step: Int) = when (step) {
    1 -> "Tell us how to identify and reach you."
    2 -> "Add the details that help us verify your student membership."
    3 -> "A clear photo helps coordinators and members recognize you."
    4 -> "Share your interests. Your preference is not an assignment."
    else -> "Check everything once before it goes to the coordinator."
}

private fun ordinal(year: Int) = when (year) { 1 -> "st"; 2 -> "nd"; 3 -> "rd"; else -> "th" }
