package dcrustm.ecell.mobile.domain.model

enum class MembershipApplicationStatus {
    INCOMPLETE,
    PENDING,
    NEEDS_CORRECTION,
    REJECTED,
    APPROVED;

    companion object {
        fun fromStoredValue(value: String?): MembershipApplicationStatus =
            entries.firstOrNull { it.name == value } ?: PENDING
    }
}

/** Private application details. Enrollment numbers never belong in users/{uid}. */
data class MemberApplication(
    val uid: String = "",
    val enrollmentNumber: String = "",
    val section: String = "",
    val preferredCommittee: Committee = Committee.NONE,
    val status: MembershipApplicationStatus = MembershipApplicationStatus.PENDING,
    val profileCompleted: Boolean = true,
    val feedback: String = "",
    val activeStep: Int = 1,
    val memberId: String = "",
    val submittedAt: Long = 0L
)

data class PendingMemberApplication(
    val member: Member,
    val application: MemberApplication
)

data class MemberIdVerification(
    val memberId: String,
    val name: String,
    val branch: String,
    val year: Int,
    val committee: Committee,
    val role: ROLE
)

data class ProfileDraft(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val year: Int = 1,
    val branch: String = "",
    val section: String = "",
    val enrollmentNumber: String = "",
    val preferredCommittee: Committee = Committee.NONE,
    val bio: String = "",
    val profilePhotoPath: String = "",
    val activeStep: Int = 1
)
