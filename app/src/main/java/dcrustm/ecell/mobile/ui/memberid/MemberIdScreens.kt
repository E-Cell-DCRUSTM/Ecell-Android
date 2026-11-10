package dcrustm.ecell.mobile.ui.memberid

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode
import dcrustm.ecell.mobile.domain.model.MemberIdVerification
import dcrustm.ecell.mobile.domain.model.ROLE
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberIdScreen(onBack: () -> Unit, viewModel: MemberIdViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val exporter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/png")) { uri ->
        val bitmap = state.card
        if (uri != null && bitmap != null) {
            runCatching { writePng(context, uri, bitmap) }
                .onSuccess { scope.launch { snackbar.showSnackbar("Member ID image saved.") } }
                .onFailure { scope.launch { snackbar.showSnackbar("Could not save the image. Please try again.") } }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("E-Cell Member ID") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                state.isLoading -> CircularProgressIndicator()
                state.card != null -> {
                    Image(
                        bitmap = state.card!!.asImageBitmap(),
                        contentDescription = "Approved E-Cell member ID with QR verification code",
                        modifier = Modifier.fillMaxWidth().aspectRatio(1.588f),
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                    )
                    Text("This confirms E-Cell membership only. It is not a university identity card.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = { exporter.launch("e-cell-member-id-${state.memberId}.png") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Outlined.Download, contentDescription = null)
                            Spacer(Modifier.size(6.dp))
                            Text("Save image")
                        }
                        Button(
                            onClick = {
                                runCatching { shareCard(context, state.card!!, state.memberId) }
                                    .onFailure { scope.launch { snackbar.showSnackbar("Could not open the share sheet. Try saving the image instead.") } }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Outlined.Share, contentDescription = null)
                            Spacer(Modifier.size(6.dp))
                            Text("Share ID")
                        }
                    }
                    Text("The QR contains only your member ID. A coordinator can use it to confirm your approved membership.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                else -> {
                    Text(state.errorMessage ?: "Could not prepare your member ID.",
                        color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                    OutlinedButton(onClick = viewModel::retry) { Text("Retry") }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun VerifyMemberIdScreen(onBack: () -> Unit, viewModel: MemberIdVerificationViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scannerOptions = remember {
        GmsBarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Verify Member ID") }, navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                }
            })
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Verify an approved E-Cell member using the ID or its QR code.",
                style = MaterialTheme.typography.bodyLarge)
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::setQuery,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Member ID or QR value") },
                placeholder = { Text("EC-0123456789ABCDEF") },
                singleLine = true
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = {
                    GmsBarcodeScanning.getClient(context, scannerOptions).startScan()
                        .addOnSuccessListener { barcode -> viewModel.verify(barcode.rawValue.orEmpty()) }
                        .addOnFailureListener {
                            scope.launch { snackbar.showSnackbar("Scanner unavailable. Enter the ID manually.") }
                        }
                }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Outlined.QrCodeScanner, contentDescription = null)
                    Spacer(Modifier.size(6.dp))
                    Text("Scan QR")
                }
                Button(onClick = viewModel::verify, enabled = !state.isLoading,
                    modifier = Modifier.weight(1f)) {
                    Text(if (state.isLoading) "Checking…" else "Verify ID")
                }
            }
            state.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
            state.result?.let { result -> VerifiedMemberCard(result) }
            Text("Verification returns only the member's name, branch, year, committee and role. It does not reveal contact or enrollment data.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun VerifiedMemberCard(result: MemberIdVerification) {
    androidx.compose.material3.Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text("Approved E-Cell member", style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            Text(result.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(result.memberId, style = MaterialTheme.typography.bodyMedium)
            Text("${result.branch} · Year ${result.year}", style = MaterialTheme.typography.bodyMedium)
            Text("${result.committee.displayName} · ${result.role.name.replace('_', ' ')}",
                style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private fun writePng(context: Context, uri: Uri, bitmap: Bitmap) {
    checkNotNull(context.contentResolver.openOutputStream(uri)).use { output ->
        check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) { "Could not write the PNG image." }
    }
}

private fun shareCard(context: Context, bitmap: Bitmap, memberId: String) {
    val shareDirectory = File(context.cacheDir, "member-id-share").apply { mkdirs() }
    val file = File(shareDirectory, "e-cell-member-id-$memberId.png")
    FileOutputStream(file).use { output ->
        check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) { "Could not prepare the ID image." }
    }
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(Intent.createChooser(intent, "Share E-Cell Member ID").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}
