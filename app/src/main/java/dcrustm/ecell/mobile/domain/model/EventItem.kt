package dcrustm.ecell.mobile.domain.model

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

data class EventItem(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val startTime: Long = 0L,
    val location: String = "",
    val isVirtual: Boolean = false,
    val meetingLink: String = "",
    val committee: Committee? = null,
    val createdByUid: String = "",
    val createdByName: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    val isOrgWide: Boolean get() = committee == null

    val audienceLabel: String get() = committee?.displayName ?: "All members"

    private val zonedStart get() = Instant.ofEpochMilli(startTime).atZone(ZoneId.systemDefault())

    val dateLabel: String get() = zonedStart.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))

    val dayKey: String get() = zonedStart.toLocalDate().toString()

    val timeLabel: String get() = zonedStart.format(DateTimeFormatter.ofPattern("h:mm a"))

    val isPast: Boolean get() = startTime < System.currentTimeMillis()
}
