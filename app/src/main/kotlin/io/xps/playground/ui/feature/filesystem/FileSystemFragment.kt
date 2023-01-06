package io.xps.playground.ui.feature.filesystem

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.os.storage.StorageManager
import android.os.storage.StorageManager.ACTION_MANAGE_STORAGE
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import io.xps.playground.R
import io.xps.playground.databinding.FragmentComposeBinding
import io.xps.playground.tools.byteSizeToString
import io.xps.playground.tools.viewBinding
import io.xps.playground.ui.composables.ScreenTittle
import io.xps.playground.ui.theme.PlaygroundTheme
import okio.buffer
import okio.source
import java.io.File
import java.security.SecureRandom
import java.util.*
import kotlin.math.roundToInt

@AndroidEntryPoint
class FileSystemFragment: Fragment(R.layout.fragment_compose) {

    private val binding by viewBinding(FragmentComposeBinding::bind)
    private val viewModel by viewModels<FileSystemViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.containerCompose.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        binding.containerCompose.setContent {
            PlaygroundTheme {
                FileSystemScreen()
            }
        }

        val bytes = generateBytes(viewModel.sampleDataSize)
        val byteBuffer = bytes.inputStream().source().buffer().readByteString()
        Log.d("byteBuffer sha256", byteBuffer.md5().hex())
    }

    private fun queryFreeSpace(internalStorage: Boolean): Pair<Long, Long> {
        val storageDirectory = if(internalStorage) {
            Environment.getDataDirectory()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.getStorageDirectory()
        } else {
            Environment.getExternalStorageDirectory()
        }

        val stats = StatFs(storageDirectory!!.path)
        val blockSize = stats.blockSizeLong
        val availableSpace = (stats.availableBlocksLong * blockSize)
        val totalSpace = (stats.blockCountLong * blockSize)
        return Pair(availableSpace, totalSpace)
    }

    private fun generateBytes(mbSize: Float): ByteArray {
        val byteSize = (mbSize * 1024 * 1024).roundToInt()
        val array = ByteArray(byteSize)
        SecureRandom().nextBytes(array)
        return array
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun allocateFreeSpace(context: Context, mb: Float){
        val bytesNeeded = (1024 * 1024 * mb).toLong()
        val storageManager = context.getSystemService<StorageManager>()!!
        val appSpecificInternalDirUuid: UUID = storageManager.getUuidForPath(context.filesDir)
        val availableBytes: Long = storageManager.getAllocatableBytes(appSpecificInternalDirUuid)
        if (availableBytes >= bytesNeeded) {
            storageManager.allocateBytes(appSpecificInternalDirUuid, bytesNeeded)
        } else {
            val storageIntent = Intent().apply {
                // To request that the user remove all app cache files instead, set
                // "action" to ACTION_CLEAR_APP_CACHE.
                action = ACTION_MANAGE_STORAGE
            }
        }
    }






    @Composable
    fun FileSystemScreen() {
        Surface {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 24.dp)
            ) {
                item { ScreenTittle(text = "Storage") }
                item {
                    val (available, total) = queryFreeSpace(true)
                    StorageItem(true, available, total)
                }
                item {
                    val (available, total) = queryFreeSpace(false)
                    StorageItem(false, available, total)
                }
                item {
                    val context = LocalContext.current
                    AccessTestItem("Internal Storage", context.filesDir)
                }
            }
        }
    }

    @Composable
    fun StorageItem(internalStorage: Boolean, available: Long, total: Long) {
        Column {
            val used = total - available
            Text(
                text = if (internalStorage) "Internal" else "External",
                style = MaterialTheme.typography.bodyMedium
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = used.byteSizeToString(false).filter { it.isDigit() },
                    style = MaterialTheme.typography.titleLarge
                )
                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                    Row {
                        Text(
                            modifier = Modifier.padding(start = 4.dp),
                            text = "${ used.byteSizeToString(false)
                                .filter { !it.isDigit() && it.isLetter()}} used",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "${total.byteSizeToString(false)
                                .replace(" ", "")
                            } total",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(15.dp)
                    .clip(RoundedCornerShape(50)),
                progress = (used.toDouble() / total.toDouble()).toFloat()
            )
        }
    }

    //TODO Write Actual Test
    @Composable
    fun AccessTestItem(tittle: String, target: File) {
        Text(
            modifier = Modifier.padding(vertical = 16.dp),
            text = tittle,
            style = MaterialTheme.typography.bodyMedium
        )

        Column(
            modifier = Modifier
                .border(
                    width = 4.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(12)
                )
                .padding(16.dp),
        ) {
            val start = remember { mutableStateOf(false)}
            AnimatedVisibility(
                visible = start.value,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column{
                    ActionItem("Write", ActionItemState.Success("k12h3jkh31kjh21kj1321"))
                    ActionItem("Read", ActionItemState.Error("k12h3jkh31kjh21kj1321"))
                }
            }
            AnimatedVisibility(
                visible = !start.value,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    modifier = Modifier
                        .clickable { start.value = !start.value }
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    text = "Tap to start",
                    textAlign = TextAlign.Center
                )
            }
        }
    }
    
    @Composable
    fun ActionItem(tittle: String, state: ActionItemState){
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp),
        ) {
            Column {
                Text(
                    modifier = Modifier.padding(top = 4.dp),
                    text = tittle,
                    style = MaterialTheme.typography.bodyMedium
                )
                when(state){
                    is ActionItemState.Success -> {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    is ActionItemState.Error -> {
                        Text(
                            text = state.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    is ActionItemState.Loading -> {}
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            when(state){
                is ActionItemState.Success -> {
                    Image(
                        modifier = Modifier.size(30.dp),
                        painter = painterResource(id = R.drawable.ic_check),
                        contentDescription = null
                    )
                }
                is ActionItemState.Error -> {
                    Image(
                        modifier = Modifier.size(30.dp),
                        painter = painterResource(id = R.drawable.ic_error),
                        contentDescription = null
                    )
                }
                is ActionItemState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier) 
                }
            }
        }
    }
    
    sealed class ActionItemState {
        data class Success(val message: String): ActionItemState()
        data class Error(val error: String): ActionItemState()
        object Loading: ActionItemState()
    }
}
