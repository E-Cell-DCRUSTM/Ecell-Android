package dcrustm.ecell.mobile.domain.model

data class Complaint(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val filerUid: String = "",
    val filerName: String = "",
    val filerCommittee: Committee = Committee.NONE,
    val status: ComplaintStatus = ComplaintStatus.OPEN,
    val response: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    val timeAgo: String get() {
        val diff = System.currentTimeMillis() - createdAt
        val days = diff / 86_400_000
        val hours = diff / 3_600_000
        return when {
            hours < 1 -> "just now"
            hours < 24 -> "${hours}h ago"
            days == 1L -> "yesterday"
            else -> "${days}d ago"
        }
    }
}

enum class ComplaintStatus(val label: String) {
    OPEN("Open"),
    IN_REVIEW("In Review"),
    RESOLVED("Resolved")
}
