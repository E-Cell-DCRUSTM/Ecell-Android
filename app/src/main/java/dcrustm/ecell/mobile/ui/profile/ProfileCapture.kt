package dcrustm.ecell.mobile.ui.profile

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

fun createProfileCaptureUri(context: Context): Uri {
    val directory = File(context.cacheDir, "profile-captures").apply { mkdirs() }
    val file = File(directory, "profile-${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}
