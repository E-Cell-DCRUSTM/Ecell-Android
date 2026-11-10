package dcrustm.ecell.mobile.data.firebase

import com.google.android.gms.tasks.Task
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

suspend fun <T> Task<T>.awaitResult(): T = suspendCancellableCoroutine { cont ->
    addOnCompleteListener { task ->
        val exception = task.exception
        when {
            task.isCanceled -> cont.cancel()
            exception != null -> cont.resumeWithException(exception)
            else -> cont.resume(task.result)
        }
    }
}
