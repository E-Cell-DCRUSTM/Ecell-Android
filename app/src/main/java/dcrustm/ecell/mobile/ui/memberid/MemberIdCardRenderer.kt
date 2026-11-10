package dcrustm.ecell.mobile.ui.memberid

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.google.zxing.qrcode.QRCodeWriter
import dcrustm.ecell.mobile.domain.model.Member
import java.util.EnumMap

object MemberIdPayload {
    private const val PREFIX = "ECELL-ID:"
    private val idPattern = Regex("EC-[0-9A-F]{16}")

    fun encode(memberId: String): String {
        val normalized = memberId.trim().uppercase()
        require(idPattern.matches(normalized)) { "This member ID is not valid." }
        return PREFIX + normalized
    }

    fun decode(payload: String): String? = payload.trim().uppercase()
        .removePrefix(PREFIX)
        .takeIf(idPattern::matches)
}

fun renderMemberIdCard(member: Member, memberId: String, profilePhoto: Bitmap?): Bitmap {
    val payload = MemberIdPayload.encode(memberId)
    val width = 1080
    val height = 680
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val card = RectF(0f, 0f, width.toFloat(), height.toFloat())
    val background = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = LinearGradient(0f, 0f, width.toFloat(), height.toFloat(),
            intArrayOf(0xFF111B3A.toInt(), 0xFF163D68.toInt(), 0xFF126C84.toInt()),
            null, Shader.TileMode.CLAMP)
    }
    canvas.drawRoundRect(card, 36f, 36f, background)
    val plain = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFFFFFF.toInt() }
    val muted = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFB9CBE1.toInt() }
    val accent = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF43C5FF.toInt() }
    val line = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x33FFFFFF }

    canvas.drawRoundRect(RectF(0f, 0f, width.toFloat(), 12f), 8f, 8f, accent)
    text(canvas, "E-CELL", 60f, 76f, 38f, plain, bold = true)
    text(canvas, "DCRUSTM  ·  MEMBER ID", 60f, 112f, 21f, muted, bold = true)
    canvas.drawLine(60f, 136f, 1020f, 136f, line)

    val photoRect = RectF(60f, 166f, 346f, 466f)
    val photoPath = Path().apply { addRoundRect(photoRect, 24f, 24f, Path.Direction.CW) }
    canvas.save()
    canvas.clipPath(photoPath)
    if (profilePhoto != null && !profilePhoto.isRecycled) {
        canvas.drawBitmap(profilePhoto, null, photoRect, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
    } else {
        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(photoRect.left, photoRect.top, photoRect.right, photoRect.bottom,
                0xFF347DA8.toInt(), 0xFF244272.toInt(), Shader.TileMode.CLAMP)
        }
        canvas.drawRect(photoRect, fill)
        val initials = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFFFFF.toInt()
            textSize = 86f
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(member.initials, photoRect.centerX(), photoRect.centerY() + 28f, initials)
    }
    canvas.restore()

    text(canvas, member.name.take(38), 384f, 206f, 39f, plain, bold = true)
    text(canvas, memberId, 384f, 252f, 25f, accent, bold = true)
    text(canvas, "APPROVED E-CELL MEMBER", 384f, 296f, 18f, muted, bold = true)
    canvas.drawLine(384f, 320f, 1016f, 320f, line)

    text(canvas, "DEPARTMENT", 384f, 358f, 16f, muted, bold = true)
    text(canvas, member.branch.ifBlank { "—" }.take(30), 384f, 390f, 25f, plain)
    text(canvas, "ACADEMIC YEAR", 384f, 436f, 16f, muted, bold = true)
    text(canvas, member.yearLabel, 384f, 468f, 24f, plain)
    text(canvas, "COMMITTEE  ·  ROLE", 384f, 514f, 16f, muted, bold = true)
    text(canvas, "${member.committee.displayName}  ·  ${member.roleLabel}".take(42), 384f, 546f, 20f, plain)

    val qr = createMemberQr(payload, 184)
    val qrRect = RectF(812f, 372f, 1012f, 572f)
    val qrBacking = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFFFFFF.toInt() }
    canvas.drawRoundRect(RectF(802f, 362f, 1022f, 582f), 16f, 16f, qrBacking)
    canvas.drawBitmap(qr, null, qrRect, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
    qr.recycle()
    text(canvas, "SCAN TO VERIFY", 832f, 608f, 15f, muted, bold = true)
    canvas.drawLine(60f, 626f, 1020f, 626f, line)
    text(canvas, "E-CELL MEMBERSHIP CARD  ·  NOT A DCRUST UNIVERSITY ID", 60f, 656f, 15f, muted, bold = true)
    background.shader = null
    return bitmap
}

fun createMemberQr(payload: String, size: Int = 256): Bitmap {
    val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java).apply {
        put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M)
        put(EncodeHintType.MARGIN, 1)
    }
    val matrix = QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, size, size, hints)
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    for (y in 0 until size) for (x in 0 until size) {
        bitmap.setPixel(x, y, if (matrix[x, y]) 0xFF111827.toInt() else 0xFFFFFFFF.toInt())
    }
    return bitmap
}

private fun text(canvas: Canvas, value: String, x: Float, y: Float, size: Float, paint: Paint, bold: Boolean = false) {
    paint.textSize = size
    paint.typeface = Typeface.create("sans-serif", if (bold) Typeface.BOLD else Typeface.NORMAL)
    canvas.drawText(value, x, y, paint)
}
