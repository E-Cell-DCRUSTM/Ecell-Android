package dcrustm.ecell.mobile.domain.model

data class Poll(
    val id: String,
    val title: String,
    val question: String,
    val options: List<String>,
    /** `ALL` or a Committee enum name. */
    val audienceCommittee: String,
    val createdByUid: String,
    val createdByName: String,
    val isOpen: Boolean,
    val createdAt: Long,
    val hasVoted: Boolean = false,
    val tallies: List<Int> = emptyList()
) {
    val totalVotes: Int get() = tallies.sum()
    val isOrgWide: Boolean get() = audienceCommittee == "ALL"
}
