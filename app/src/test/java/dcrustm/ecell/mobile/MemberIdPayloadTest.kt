package dcrustm.ecell.mobile

import dcrustm.ecell.mobile.ui.memberid.MemberIdPayload
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MemberIdPayloadTest {
    private val id = "EC-0123456789ABCDEF"

    @Test
    fun qrPayloadContainsOnlyTheOpaqueMemberId() {
        val payload = MemberIdPayload.encode(id)
        assertEquals("ECELL-ID:$id", payload)
        assertEquals(id, MemberIdPayload.decode(payload))
        assertEquals(id, MemberIdPayload.decode(id))
    }

    @Test(expected = IllegalArgumentException::class)
    fun payloadCannotBeGeneratedFromMalformedOrSensitiveData() {
        MemberIdPayload.encode("student@example.org;enrollment=123;phone=555")
    }

    @Test
    fun verificationRejectsUnexpectedQrContents() {
        assertNull(MemberIdPayload.decode("https://example.org/profile/123"))
        assertNull(MemberIdPayload.decode("EC-ABC"))
    }
}
