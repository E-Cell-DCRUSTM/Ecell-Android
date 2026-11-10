package dcrustm.ecell.mobile.domain.validation

import dcrustm.ecell.mobile.domain.model.ProfileDraft

data class ProfileFieldRequirements(
    val phoneRequired: Boolean = false,
    val enrollmentNumberRequired: Boolean = false
)

object ProfileDraftValidator {
    private val emailPattern = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
    private val phoneCharacters = Regex("^[+0-9() .-]+$")

    fun isValidEmail(value: String): Boolean {
        return value.isNotEmpty() && value.length <= 254 && emailPattern.matches(value)
    }

    fun isValidPhone(value: String, required: Boolean = false): Boolean {
        val phone = value.trim()
        val digits = phone.count { it in '0'..'9' }
        return if (phone.isBlank()) !required
        else phone.length <= 32 && phoneCharacters.matches(phone) && digits in 7..15
    }

    fun validatePhotoPath(path: String, uid: String): Boolean =
        path.isBlank() || path == "memberPhotos/$uid/profile.jpg"

    /** Required fields are intentionally limited to identity, department and academic year. */
    fun validate(
        draft: ProfileDraft,
        uid: String,
        requirements: ProfileFieldRequirements = ProfileFieldRequirements()
    ): Map<String, String> = buildMap {
        validateIdentity(draft, requirements, required = true)
        validateAcademic(draft, requirements, required = true)
        validateOptionalFields(draft, uid)
    }

    /** Structural checks used for autosave; blank required fields are allowed until submit. */
    fun validateDraft(draft: ProfileDraft, uid: String): Map<String, String> = buildMap {
        if (draft.name.isNotBlank() && draft.name.trim().length !in 2..100) {
            put("name", "Full name must be 2–100 characters.")
        }
        if (!isValidEmail(draft.email)) {
            put("email", "Enter a valid email address.")
        }
        validatePhone(draft.phone, required = false)
        if (draft.year !in 1..4) put("year", "Choose an academic year from 1st to 4th.")
        if (draft.branch.length > 100) put("branch", "Department or branch must be 100 characters or fewer.")
        if (draft.enrollmentNumber.trim().length > 40) {
            put("enrollmentNumber", "Enrollment number must be 40 characters or fewer.")
        }
        if (draft.section.length > 40) put("section", "Section must be 40 characters or fewer.")
        validateOptionalFields(draft, uid)
    }

    fun validateStep(
        step: Int,
        draft: ProfileDraft,
        uid: String,
        requirements: ProfileFieldRequirements = ProfileFieldRequirements()
    ): Map<String, String> = when (step) {
        1 -> buildMap { validateIdentity(draft, requirements, required = true) }
        2 -> buildMap { validateAcademic(draft, requirements, required = true) }
        3 -> buildMap { validatePhoto(draft, uid) }
        4 -> buildMap { if (draft.bio.length > 1000) put("bio", "Introduction must be 1,000 characters or fewer.") }
        5 -> validate(draft, uid, requirements)
        else -> mapOf("step" to "Return to a valid profile setup step.")
    }

    private fun MutableMap<String, String>.validateIdentity(
        draft: ProfileDraft,
        requirements: ProfileFieldRequirements,
        required: Boolean
    ) {
        val name = draft.name.trim()
        if ((required && name.length !in 2..100) || (!required && name.isNotBlank() && name.length !in 2..100)) {
            put("name", "Enter your full name (2–100 characters).")
        }
        if (!isValidEmail(draft.email)) put("email", "Enter a valid email address.")
        validatePhone(draft.phone, requirements.phoneRequired && required)
    }

    private fun MutableMap<String, String>.validateAcademic(
        draft: ProfileDraft,
        requirements: ProfileFieldRequirements,
        required: Boolean
    ) {
        if (draft.year !in 1..4) put("year", "Choose an academic year from 1st to 4th.")
        val branch = draft.branch.trim()
        if ((required && branch.isBlank()) || branch.length > 100) {
            put("branch", "Enter your department or branch (up to 100 characters).")
        }
        val enrollment = draft.enrollmentNumber.trim()
        if (enrollment.isBlank() && requirements.enrollmentNumberRequired && required) {
            put("enrollmentNumber", "Enter your university enrollment number.")
        } else if (enrollment.length > 40) {
            put("enrollmentNumber", "Enrollment number must be 40 characters or fewer.")
        }
        if (draft.section.length > 40) put("section", "Section must be 40 characters or fewer.")
    }

    private fun MutableMap<String, String>.validatePhone(value: String, required: Boolean) {
        if (!isValidPhone(value, required)) {
            if (value.isBlank() && required) {
                put("phone", "Enter a contact number.")
            } else {
                put("phone", "Enter a valid phone number with 7–15 digits.")
            }
        }
    }

    private fun MutableMap<String, String>.validateOptionalFields(draft: ProfileDraft, uid: String) {
        if (draft.bio.length > 1000) put("bio", "Introduction must be 1,000 characters or fewer.")
        validatePhoto(draft, uid)
        if (draft.activeStep !in 1..5) put("step", "Return to a valid profile setup step.")
    }

    private fun MutableMap<String, String>.validatePhoto(draft: ProfileDraft, uid: String) {
        if (!validatePhotoPath(draft.profilePhotoPath, uid)) {
            put("profilePhoto", "Choose a profile photo uploaded from this account.")
        }
    }
}
