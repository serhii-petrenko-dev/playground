package io.xps.playground.ui.feature.imagePicker

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.ext.SdkExtensions
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import dagger.hilt.android.AndroidEntryPoint
import io.xps.playground.R
import io.xps.playground.databinding.FragmentComposeBinding
import io.xps.playground.extensions.toast
import io.xps.playground.extensions.vibrate
import io.xps.playground.tools.OnClick
import io.xps.playground.tools.viewBinding
import io.xps.playground.ui.theme.PlaygroundTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ImagePickerFragment : Fragment(R.layout.fragment_compose) {

    private val binding by viewBinding(FragmentComposeBinding::bind)
    private val viewModel by viewModels<ImagePickerViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.containerCompose.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )

        val mediaContract = ActivityResultContracts.PickVisualMedia()
        val pickMedia = registerForActivityResult(mediaContract) { uri ->
            if (uri != null) {
                viewModel.storeUri(uri)
                if (viewModel.persistMediaAccess.value) {
                    persistUriAccess(uri)
                }
            } else {
                binding.root.context.toast(R.string.no_media)
            }
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.imageUri.first()?.let { uri ->
                    viewModel.mediaAccess(isUriAccessPersisted(uri))
                }
            }
        }

        binding.containerCompose.setContent {
            PlaygroundTheme {
                val imageUri = viewModel.imageUri.collectAsStateWithLifecycle(null)
                BaseScreen(
                    imageUri = imageUri.value,
                    onClick = {
                        val contractType = ActivityResultContracts.PickVisualMedia.ImageOnly
                        val mediaRequest = PickVisualMediaRequest(contractType)
                        if (isPhotoPickerAvailable()) {
                            pickMedia.launch(mediaRequest)
                        } else {
                            binding.root.context.toast(R.string.no_image_picker)
                        }
                    },
                    onLongClick = {
                        binding.root.context.vibrate()
                        imageUri.value?.let { uri ->
                            releaseUriAccess(uri)
                        }
                        viewModel.storeUri(null)
                    }
                )
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun BaseScreen(imageUri: Uri?, onClick: OnClick, onLongClick: OnClick) {
        var hasImage by remember { mutableStateOf(false) }
        Surface(modifier = Modifier.fillMaxSize()) {
            val persistMediaAccess = viewModel.persistMediaAccess.collectAsState()
            Column(
                modifier = Modifier.wrapContentHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(10))
                        .border(
                            width = 6.dp,
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(10)
                        )
                        .combinedClickable(
                            onClick = onClick,
                            onLongClick = onLongClick
                        )
                ) {
                    hasImage = imageUri != null
                    Image(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxSize(if (hasImage) 1f else 0.3f)
                            .clip(RoundedCornerShape(16))
                            .padding(6.dp),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        painter = if (imageUri != null) {
                            rememberAsyncImagePainter(
                                model = ImageRequest
                                    .Builder(LocalContext.current)
                                    .data(data = imageUri)
                                    .error(R.drawable.ic_image_search)
                                    .build(),
                                onState = {
                                    when (it) {
                                        is AsyncImagePainter.State.Success -> {
                                            hasImage = true
                                        }

                                        is AsyncImagePainter.State.Error -> {
                                            viewModel.storeUri(null)
                                            binding.root.context.toast(R.string.media_error)
                                        }

                                        else -> {}
                                    }
                                }
                            )
                        } else {
                            painterResource(id = R.drawable.ic_image_search)
                        }
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = persistMediaAccess.value,
                        onCheckedChange = {
                            viewModel.mediaAccess(it)
                            if (imageUri != null) {
                                if (it) {
                                    persistUriAccess(imageUri)
                                } else {
                                    releaseUriAccess(imageUri)
                                }
                            }
                        }
                    )
                    Text(
                        text = stringResource(id = R.string.persist_access),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }

    private fun persistUriAccess(uri: Uri) {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
                requireContext().contentResolver.takePersistableUriPermission(uri, flag)
            }
        }
    }

    private fun releaseUriAccess(uri: Uri) {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
                requireContext().contentResolver.releasePersistableUriPermission(uri, flag)
            }
        }
    }

    private fun isUriAccessPersisted(uri: Uri): Boolean {
        return requireContext().contentResolver.persistedUriPermissions.firstOrNull {
            it.uri == uri && it.isReadPermission
        } != null
    }

    @SuppressLint("NewApi") // https://issuetracker.google.com/issues/257912685?pli=1
    private fun isPhotoPickerAvailable(): Boolean {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> true
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                SdkExtensions.getExtensionVersion(Build.VERSION_CODES.R) >= 2
            }
            else -> false
        }
    }
}
