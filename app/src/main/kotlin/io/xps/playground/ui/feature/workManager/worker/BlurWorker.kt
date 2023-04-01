package io.xps.playground.ui.feature.workManager.worker

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.text.TextUtils
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.delay
import logcat.LogPriority
import logcat.logcat

class BlurWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val appContext = applicationContext
        val resourceUri = inputData.getString(KEY_IMAGE_URI)

        makeStatusNotification("Blurring image", appContext)

        (0..100 step 10).forEach {
            setProgressAsync(workDataOf(PROGRESS to it))
            delay(TIMEOUT)
        }

        return try {
            if (TextUtils.isEmpty(resourceUri)) {
                logcat { "Invalid input uri" }
                throw IllegalArgumentException("Invalid input uri")
            }

            val resolver = appContext.contentResolver
            val picture = BitmapFactory.decodeStream(
                resolver.openInputStream(Uri.parse(resourceUri))
            )
            val output = blurBitmap(picture, appContext)

            val outputUri = writeBitmapToFile(appContext, output)
            val outputData = workDataOf(KEY_IMAGE_URI to outputUri.toString())
            Result.success(outputData)
        } catch (throwable: Throwable) {
            logcat(LogPriority.ERROR) { "Error applying blur" }
            Result.failure()
        }
    }
}
