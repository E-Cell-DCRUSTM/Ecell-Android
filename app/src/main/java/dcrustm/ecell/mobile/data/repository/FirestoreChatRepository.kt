package dcrustm.ecell.mobile.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dcrustm.ecell.mobile.data.firebase.awaitResult
import dcrustm.ecell.mobile.data.firebase.snapshots
import dcrustm.ecell.mobile.domain.model.Conversation
import dcrustm.ecell.mobile.domain.model.DirectMessage
import dcrustm.ecell.mobile.domain.model.Member
import dcrustm.ecell.mobile.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FirestoreChatRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : ChatRepository {

    override fun observeConversations(uid: String): Flow<List<Conversation>> =
        firestore.collection("conversations")
            .whereArrayContains("participants", uid)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.map { document ->
                    Conversation(
                        id = document.id,
                        participants = (document.get("participants") as? List<*>)
                            ?.filterIsInstance<String>()
                            .orEmpty(),
                        lastMessage = document.getString("lastMessage") ?: "",
                        lastSenderUid = document.getString("lastSenderUid") ?: "",
                        lastMessageId = document.getString("lastMessageId") ?: "",
                        updatedAt = document.getTimestamp("updatedAt")?.toDate()?.time ?: 0L
                    )
                }.sortedByDescending(Conversation::updatedAt)
            }

    override fun observeMessages(conversationId: String): Flow<List<DirectMessage>> =
        firestore.collection("conversations").document(conversationId)
            .collection("messages")
            .orderBy("sentAt", Query.Direction.ASCENDING)
            .limitToLast(100)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { document ->
                    val text = document.getString("text") ?: return@mapNotNull null
                    DirectMessage(
                        id = document.id,
                        senderUid = document.getString("senderUid") ?: "",
                        text = text,
                        sentAt = document.getTimestamp("sentAt")?.toDate()?.time ?: 0L
                    )
                }
            }

    override suspend fun openConversation(first: Member, second: Member): String {
        require(first.uid.isNotBlank() && second.uid.isNotBlank() && first.uid != second.uid)
        val participants = listOf(first.uid, second.uid).sorted()
        val conversationId = participants.joinToString("_")
        val ref = firestore.collection("conversations").document(conversationId)

        findConversation(first.uid, participants)?.let { return it }

        try {
            ref.set(mapOf(
                "participants" to participants,
                "createdByUid" to first.uid,
                "createdAt" to FieldValue.serverTimestamp(),
                "lastMessage" to "",
                "lastSenderUid" to "",
                "lastMessageId" to "",
                "updatedAt" to FieldValue.serverTimestamp()
            )).awaitResult()
        } catch (writeError: Exception) {
            // Two participants may open the same new thread at once. The
            // canonical create rule lets one win; the other then reads it only
            // through the participant-filtered query and reuses its ID.
            if (findConversation(first.uid, participants) != conversationId) throw writeError
        }
        return conversationId
    }

    private suspend fun findConversation(
        currentUid: String,
        participants: List<String>
    ): String? = firestore.collection("conversations")
        .whereArrayContains("participants", currentUid)
        .get()
        .awaitResult()
        .documents
        .firstOrNull { it.get("participants") == participants }
        ?.id

    override suspend fun sendMessage(conversationId: String, senderUid: String, text: String) {
        val content = text.trim()
        require(content.isNotEmpty() && content.length <= 4000)

        val conversation = firestore.collection("conversations").document(conversationId)
        val message = conversation.collection("messages").document()
        val batch = firestore.batch()
        batch.set(message, mapOf(
            "senderUid" to senderUid,
            "text" to content,
            "sentAt" to FieldValue.serverTimestamp()
        ))
        batch.update(conversation, mapOf(
            "lastMessage" to content,
            "lastSenderUid" to senderUid,
            "lastMessageId" to message.id,
            "updatedAt" to FieldValue.serverTimestamp()
        ))
        batch.commit().awaitResult()
    }
}
