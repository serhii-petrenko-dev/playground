package io.xps.playground.ui.feature.workManager

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.xps.playground.R
import io.xps.playground.tools.NavigationDispatcher
import io.xps.playground.ui.feature.workManager.worker.BlurWorker
import io.xps.playground.ui.feature.workManager.worker.CleanupWorker
import io.xps.playground.ui.feature.workManager.worker.IMAGE_BLUR_WORK
import io.xps.playground.ui.feature.workManager.worker.KEY_IMAGE_URI
import io.xps.playground.ui.feature.workManager.worker.SaveImageToFileWorker
import io.xps.playground.ui.feature.workManager.worker.WORK_TAG_OUTPUT
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import kotlin.math.roundToInt

@SuppressLint("StaticFieldLeak")
@HiltViewModel
class WorkManagerViewModel @Inject constructor(
    @ApplicationContext private val application: Context,
    private val navigationDispatcher: NavigationDispatcher
) : ViewModel() {

    private var imageUri = getImageUri(application)

    private val workManager = WorkManager.getInstance(application)
    internal val workInfo = workManager.getWorkInfosByTagLiveData(WORK_TAG_OUTPUT)

    private val _workRunning = MutableStateFlow(false)
    internal val workRunning = _workRunning.asStateFlow()

    private val _blurAmount = MutableStateFlow(0f)
    internal val blurAmount = _blurAmount.asStateFlow()

    private val _outputUri = MutableStateFlow<Uri?>(null)
    internal val outputUri = _outputUri.asStateFlow()

    fun workStatus(running: Boolean, outputUri: String? = null) {
        _workRunning.value = running
        outputUri?.let { _outputUri.value = Uri.parse(it) }
    }

    fun blurChange(amount: Float) {
        _blurAmount.value = amount
        applyBlur(amount.roundToInt(), imageUri)
    }

    fun cancel() {
        workManager.cancelAllWorkByTag(WORK_TAG_OUTPUT)
    }

    private fun applyBlur(blurLevel: Int, imageUri: Uri) {
        var continuation = workManager
            .beginUniqueWork(
                IMAGE_BLUR_WORK,
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequest.from(CleanupWorker::class.java)
            )

        repeat(blurLevel) {
            val blurRequest = OneTimeWorkRequestBuilder<BlurWorker>()
            if (it == 0) blurRequest.setInputData(createInputDataForUri(imageUri))
            continuation = continuation.then(blurRequest.build())
        }

        val constraints = Constraints.Builder()
            .setRequiresCharging(false)
            .build()
        val saveRequest = OneTimeWorkRequest
            .Builder(SaveImageToFileWorker::class.java)
            .addTag(WORK_TAG_OUTPUT)
            .setConstraints(constraints)
            .build()
        continuation = continuation.then(saveRequest)

        continuation.enqueue()
    }

    private fun createInputDataForUri(imageUri: Uri) = Data.Builder()
        .putString(KEY_IMAGE_URI, imageUri.toString())
        .build()

    private fun getImageUri(context: Context): Uri {
        val resources = context.resources

        return Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(resources.getResourcePackageName(R.drawable.android))
            .appendPath(resources.getResourceTypeName(R.drawable.android))
            .appendPath(resources.getResourceEntryName(R.drawable.android))
            .build()
    }
}
