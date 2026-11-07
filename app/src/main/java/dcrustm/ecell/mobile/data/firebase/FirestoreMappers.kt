package dcrustm.ecell.mobile.data.firebase

import com.google.firebase.firestore.DocumentSnapshot
import dcrustm.ecell.mobile.domain.model.Announcement
import dcrustm.ecell.mobile.domain.model.Committee
import dcrustm.ecell.mobile.domain.model.Complaint
import dcrustm.ecell.mobile.domain.model.ComplaintStatus
import dcrustm.ecell.mobile.domain.model.EventItem
import dcrustm.ecell.mobile.domain.model.Member
import dcrustm.ecell.mobile.domain.model.ROLE

private inline fun <reified T : Enum<T>> safeEnum(name: String?, default: T): T =
    try {
        if (name == null) default else enumValueOf<T>(name)
    } catch (e: IllegalArgumentException) {
        default
    }

fun Member.toFirestoreMap(): Map<String, Any?> = mapOf(
    "uid" to uid,
    "name" to name,
    "year" to year,
    "branch" to branch,
    "committee" to committee.name,
    "role" to role.name,
    "bio" to bio,
    "profilePhotoUrl" to profilePhotoUrl,
    "joinedAt" to joinedAt,
    "isApproved" to isApproved
)

fun DocumentSnapshot.toMember(): Member? {
    if (!exists()) return null
    return Member(
        uid = id,
        name = getString("name") ?: "",
        email = "",
        phone = "",
        year = (getLong("year") ?: 1L).toInt(),
        branch = getString("branch") ?: "",
        committee = safeEnum(getString("committee"), Committee.NONE),
        role = safeEnum(getString("role"), ROLE.PENDING),
        bio = getString("bio") ?: "",
        profilePhotoUrl = getString("profilePhotoUrl") ?: "",
        joinedAt = getLong("joinedAt") ?: 0L,
        isApproved = getBoolean("isApproved") ?: false
    )
}

fun Announcement.toFirestoreMap(): Map<String, Any?> = mapOf(
    "title" to title,
    "body" to body,
    "authorUid" to authorUid,
    "authorName" to authorName,
    "committee" to committee?.name,
    "createdAt" to createdAt,
    "isPinned" to isPinned,
    "attachmentUrl" to attachmentUrl
)

fun DocumentSnapshot.toAnnouncement(): Announcement? {
    if (!exists()) return null
    val committeeName = getString("committee")
    return Announcement(
        id = id,
        title = getString("title") ?: "",
        body = getString("body") ?: "",
        authorUid = getString("authorUid") ?: "",
        authorName = getString("authorName") ?: "",
        committee = committeeName?.let { safeEnum(it, Committee.NONE) },
        createdAt = getLong("createdAt") ?: 0L,
        isPinned = getBoolean("isPinned") ?: false,
        attachmentUrl = getString("attachmentUrl") ?: ""
    )
}

fun EventItem.toFirestoreMap(): Map<String, Any?> = mapOf(
    "title" to title,
    "description" to description,
    "startTime" to startTime,
    "location" to location,
    "isVirtual" to isVirtual,
    "meetingLink" to meetingLink,
    "committee" to committee?.name,
    "createdByUid" to createdByUid,
    "createdByName" to createdByName,
    "createdAt" to createdAt
)

fun DocumentSnapshot.toEvent(): EventItem? {
    if (!exists()) return null
    val committeeName = getString("committee")
    return EventItem(
        id = id,
        title = getString("title") ?: "",
        description = getString("description") ?: "",
        startTime = getLong("startTime") ?: 0L,
        location = getString("location") ?: "",
        isVirtual = getBoolean("isVirtual") ?: false,
        meetingLink = getString("meetingLink") ?: "",
        committee = committeeName?.let { safeEnum(it, Committee.NONE) },
        createdByUid = getString("createdByUid") ?: "",
        createdByName = getString("createdByName") ?: "",
        createdAt = getLong("createdAt") ?: 0L
    )
}

fun Complaint.toFirestoreMap(): Map<String, Any?> = mapOf(
    "title" to title,
    "body" to body,
    "filerUid" to filerUid,
    "filerName" to filerName,
    "filerCommittee" to filerCommittee.name,
    "status" to status.name,
    "response" to response,
    "createdAt" to createdAt
)

fun DocumentSnapshot.toComplaint(): Complaint? {
    if (!exists()) return null
    return Complaint(
        id = id,
        title = getString("title") ?: "",
        body = getString("body") ?: "",
        filerUid = getString("filerUid") ?: "",
        filerName = getString("filerName") ?: "",
        filerCommittee = safeEnum(getString("filerCommittee"), Committee.NONE),
        status = safeEnum(getString("status"), ComplaintStatus.OPEN),
        response = getString("response") ?: "",
        createdAt = getLong("createdAt") ?: 0L
    )
}
