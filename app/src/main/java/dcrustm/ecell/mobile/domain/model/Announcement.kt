package dcrustm.ecell.mobile.domain.model

data class Announcement(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val authorUid: String = "",
    val authorName: String = "",
    val committee: Committee? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val attachmentUrl: String = ""
) {
    val isOrgWide: Boolean get() = committee == null

    val audienceLabel: String get() = committee?.displayName ?: "All members"

    val timeAgo: String get() {
        val diff = System.currentTimeMillis() - createdAt
        val minutes = diff / 60_000
        val hours = minutes / 60
        val days = hours / 24
        return when {
            minutes < 1 -> "just now"
            minutes < 60 -> "${minutes}m ago"
            hours < 24 -> "${hours}h ago"
            days == 1L -> "yesterday"
            days < 7 -> "${days}d ago"
            else -> "${days / 7}w ago"
        }
    }
}
