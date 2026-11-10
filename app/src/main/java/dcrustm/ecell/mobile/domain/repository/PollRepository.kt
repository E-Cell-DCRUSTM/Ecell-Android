package dcrustm.ecell.mobile.domain.repository

import dcrustm.ecell.mobile.domain.model.Member
import dcrustm.ecell.mobile.domain.model.Poll
import kotlinx.coroutines.flow.Flow

interface PollRepository {
    fun observePolls(member: Member): Flow<List<Poll>>
    suspend fun createPoll(title: String, question: String, options: List<String>, audienceCommittee: String, creator: Member)
    suspend fun castVote(pollId: String, optionIndex: Int, voterUid: String)
    suspend fun closePoll(pollId: String)
}
