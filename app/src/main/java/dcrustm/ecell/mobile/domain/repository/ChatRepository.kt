package dcrustm.ecell.mobile.domain.repository

import dcrustm.ecell.mobile.domain.model.Conversation
import dcrustm.ecell.mobile.domain.model.DirectMessage
import dcrustm.ecell.mobile.domain.model.Member
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun observeConversations(uid: String): Flow<List<Conversation>>
    fun observeMessages(conversationId: String): Flow<List<DirectMessage>>
    suspend fun openConversation(first: Member, second: Member): String
    suspend fun sendMessage(conversationId: String, senderUid: String, text: String)
}
