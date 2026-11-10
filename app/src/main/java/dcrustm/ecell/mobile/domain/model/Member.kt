package dcrustm.ecell.mobile.domain.model

data class Member(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val year: Int = 1,
    val branch: String = "",
    val committee: Committee = Committee.NONE,
    val role: ROLE = ROLE.PENDING,
    val bio: String = "",
    val profilePhotoUrl: String = "",
    val joinedAt: Long = 0L,
    val isApproved: Boolean = false
) {
    val initials: String get() {
        val parts = name.trim().split(" ")
        return when {
            parts.size >= 2 -> "${parts[0].first()}${parts[1].first()}".uppercase()
            parts.isNotEmpty() -> parts[0].take(2).uppercase()
            else -> "?"
        }
    }

    val yearLabel: String get() = when (year) {
        1 -> "1st Year"
        2 -> "2nd Year"
        3 -> "3rd Year"
        4 -> "4th Year"
        else -> "Year $year"
    }

    val roleLabel: String get() = when (role) {
        ROLE.COORDINATOR -> "Coordinator"
        ROLE.COMMITTEE_HEAD -> "Committee Head"
        ROLE.MEMBER -> "Member"
        ROLE.PENDING -> "Pending Approval"
    }
}

data class MemberContact(
    val email: String = "",
    val phone: String = ""
)
