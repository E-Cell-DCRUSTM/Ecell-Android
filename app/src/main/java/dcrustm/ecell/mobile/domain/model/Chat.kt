package dcrustm.ecell.mobile.domain.model

data class Conversation(
    val id: String,
    val participants: List<String>,
    val lastMessage: String,
    val lastSenderUid: String,
    val lastMessageId: String,
    val updatedAt: Long
) {
    fun otherUid(myUid: String): String = participants.firstOrNull { it != myUid }.orEmpty()
}

data class DirectMessage(
    val id: String,
    val senderUid: String,
    val text: String,
    val sentAt: Long
)
