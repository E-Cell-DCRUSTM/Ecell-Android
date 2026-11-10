package dcrustm.ecell.mobile

import dcrustm.ecell.mobile.domain.model.Committee
import dcrustm.ecell.mobile.domain.model.ProfileDraft
import dcrustm.ecell.mobile.domain.validation.ProfileDraftValidator
import dcrustm.ecell.mobile.domain.validation.ProfileFieldRequirements
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

class ProfileDraftValidatorTest {
    private fun validDraft() = ProfileDraft(
        name = "Aarav Sharma",
        email = "aarav@dcrustm.org",
        year = 2,
        branch = "Computer Engineering",
        preferredCommittee = Committee.NONE
    )

    @Test
    fun submissionAllowsOptionalPhoneAndEnrollmentNumber() {
        assertTrue(ProfileDraftValidator.validate(validDraft(), "uid-1").isEmpty())
    }

    @Test
    fun requiredIdentityAndAcademicFieldsAreValidated() {
        val invalid = validDraft().copy(name = "A", email = "not-an-email", branch = "", year = 5)
        val errors = ProfileDraftValidator.validate(invalid, "uid-1")
        assertTrue(errors.containsKey("name"))
        assertTrue(errors.containsKey("email"))
        assertTrue(errors.containsKey("branch"))
        assertTrue(errors.containsKey("year"))
    }

    @Test
    fun optionalEnrollmentCanBeMadeRequiredByPolicy() {
        val errors = ProfileDraftValidator.validate(
            validDraft(), "uid-1",
            ProfileFieldRequirements(enrollmentNumberRequired = true)
        )
        assertTrue(errors.containsKey("enrollmentNumber"))
    }

    @Test
    fun optionalPhoneCanBeMadeRequiredByPolicy() {
        val errors = ProfileDraftValidator.validate(
            validDraft(), "uid-1", ProfileFieldRequirements(phoneRequired = true)
        )
        assertTrue(errors.containsKey("phone"))
    }

    @Test
    fun profilePhotoPathMustBelongToTheAuthenticatedMember() {
        val errors = ProfileDraftValidator.validate(
            validDraft().copy(profilePhotoPath = "memberPhotos/another-user/profile.jpg"), "uid-1"
        )
        assertTrue(errors.containsKey("profilePhoto"))
        assertTrue(ProfileDraftValidator.validatePhotoPath("memberPhotos/uid-1/profile.jpg", "uid-1"))
    }

    @Test
    fun incompleteDraftCanBeSavedBeforeRequiredFieldsAreFinished() {
        val incomplete = validDraft().copy(name = "", branch = "")
        assertTrue(ProfileDraftValidator.validateDraft(incomplete, "uid-1").isEmpty())
    }

    @Test
    fun contactValidationMatchesTheFirestoreAcceptedFormat() {
        assertTrue(ProfileDraftValidator.isValidEmail("member+test@example.edu"))
        assertFalse(ProfileDraftValidator.isValidEmail(" member+test@example.edu "))
        assertFalse(ProfileDraftValidator.isValidEmail("not-an-email"))

        assertTrue(ProfileDraftValidator.isValidPhone(""))
        assertTrue(ProfileDraftValidator.isValidPhone(" +91 (98765) 43210 "))
        assertFalse(ProfileDraftValidator.isValidPhone("555-01"))
        assertFalse(ProfileDraftValidator.isValidPhone("1234567890123456"))
        assertFalse(ProfileDraftValidator.isValidPhone("555-01AB"))
    }
}
