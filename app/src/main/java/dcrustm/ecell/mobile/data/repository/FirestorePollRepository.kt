package dcrustm.ecell.mobile.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dcrustm.ecell.mobile.data.firebase.awaitResult
import dcrustm.ecell.mobile.data.firebase.snapshots
import dcrustm.ecell.mobile.domain.model.Member
import dcrustm.ecell.mobile.domain.model.Poll
import dcrustm.ecell.mobile.domain.model.ROLE
import dcrustm.ecell.mobile.domain.repository.PollRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class FirestorePollRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : PollRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observePolls(member: Member): Flow<List<Poll>> {
        val query = if (member.role == ROLE.COORDINATOR) {
            firestore.collection("polls")
        } else {
            firestore.collection("polls").whereIn(
                "audienceCommittee",
                listOf("ALL", member.committee.name)
            )
        }

        return query.snapshots().flatMapLatest { snapshot ->
            val polls = snapshot.documents.mapNotNull { document ->
                val options = document.get("options") as? List<*> ?: return@mapNotNull null
                Poll(
                    id = document.id,
                    title = document.getString("title") ?: return@mapNotNull null,
                    question = document.getString("question") ?: "",
                    options = options.filterIsInstance<String>(),
                    audienceCommittee = document.getString("audienceCommittee") ?: "ALL",
                    createdByUid = document.getString("createdByUid") ?: "",
                    createdByName = document.getString("createdByName") ?: "E-Cell",
                    isOpen = document.getBoolean("isOpen") ?: false,
                    createdAt = document.getTimestamp("createdAt")?.toDate()?.time ?: 0L
                )
            }
            if (polls.isEmpty()) return@flatMapLatest flowOf(emptyList())

            val pollFlows = polls.map { poll ->
                val pollRef = firestore.collection("polls").document(poll.id)
                val receiptFlow = pollRef.collection("voterReceipts").document(member.uid)
                    .snapshots().map { it.exists() }
                val ballotsFlow = if (poll.isOpen) {
                    flowOf(emptyList<Int>())
                } else {
                    pollRef.collection("ballots").snapshots().map { ballots ->
                        ballots.documents.mapNotNull { it.getLong("optionIndex")?.toInt() }
                    }
                }
                combine(receiptFlow, ballotsFlow) { hasVoted, votes ->
                    val tallies = if (poll.isOpen) emptyList() else
                        List(poll.options.size) { option -> votes.count { it == option } }
                    poll.copy(hasVoted = hasVoted, tallies = tallies)
                }
            }
            combine(pollFlows) { it.toList().sortedByDescending(Poll::createdAt) }
        }
    }

    override suspend fun createPoll(
        title: String,
        question: String,
        options: List<String>,
        audienceCommittee: String,
        creator: Member
    ) {
        val data = mapOf(
            "title" to title.trim(),
            "question" to question.trim(),
            "options" to options.map(String::trim),
            "audienceCommittee" to audienceCommittee,
            "createdByUid" to creator.uid,
            "createdByName" to creator.name,
            "isOpen" to true,
            "createdAt" to FieldValue.serverTimestamp()
        )
        firestore.collection("polls").add(data).awaitResult()
    }

    override suspend fun castVote(pollId: String, optionIndex: Int, voterUid: String) {
        val pollRef = firestore.collection("polls").document(pollId)
        val ballotId = UUID.randomUUID().toString()
        val batch = firestore.batch()
        // The owner-only receipt binds this voter to exactly one random ballot ID.
        // Firestore rules require both documents to be created atomically and
        // require the ballot ID to match in both directions.
        batch.set(
            pollRef.collection("voterReceipts").document(voterUid),
            mapOf("voted" to true, "ballotId" to ballotId)
        )
        batch.set(
            pollRef.collection("ballots").document(ballotId),
            mapOf("optionIndex" to optionIndex, "castAt" to FieldValue.serverTimestamp())
        )
        batch.commit().awaitResult()
    }

    override suspend fun closePoll(pollId: String) {
        firestore.collection("polls").document(pollId).update(
            mapOf("isOpen" to false, "closedAt" to FieldValue.serverTimestamp())
        ).awaitResult()
    }
}
