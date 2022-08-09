package io.xps.playground.ui.feature.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.accompanist.permissions.*
import dagger.hilt.android.AndroidEntryPoint
import io.xps.playground.R
import io.xps.playground.databinding.FragmentComposeBinding
import io.xps.playground.tools.viewBinding
import io.xps.playground.ui.composables.BaseColumn
import io.xps.playground.ui.theme.PlaygroundTheme

@AndroidEntryPoint
class PermissionsFragment: Fragment(R.layout.fragment_compose) {

    private val binding by viewBinding(FragmentComposeBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.containerCompose.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        binding.containerCompose.setContent {
            PlaygroundTheme {
                PermissionsScreen()
            }
        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun PermissionsScreen() {
        Surface(modifier = Modifier.fillMaxSize()) {
            BaseColumn(
                modifier = Modifier.statusBarsPadding()
            ) {
                Spacer(modifier = Modifier.weight(1f))
                val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
                if (cameraPermissionState.status == PermissionStatus.Granted) {
                    ContentText("Camera permission Granted")
                } else {
                    PermissionDenied(cameraPermissionState)
                }
                Spacer(modifier = Modifier.weight(2f))
            }
        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    private fun PermissionDenied(cameraPermissionState: PermissionState){
        val permissionStatus = cameraPermissionState.status
        val textToShow = if (permissionStatus.shouldShowRationale) {
            // If the user has denied the permission but the rationale can be shown,
            // then gently explain why the app requires this permission
            "The camera is important for this app. Please grant the permission."
        } else {
            // If it's the first time the user lands on this feature, or the user
            // doesn't want to be asked again for this permission, explain that the
            // permission is required
            "Camera permission required for this feature to be available. " +
                    "Please grant the permission"
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ){
            ContentText(textToShow)
            Button(
                onClick = { cameraPermissionState.launchPermissionRequest() },
                content = {
                    Text(
                        text ="Request permission",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            )
        }
    }

    @Composable
    private fun ContentText(textToShow: String){
        Text(
            modifier = Modifier.fillMaxWidth(0.7f),
            text = textToShow,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }

    private fun checkPermission(context: Context, permission: String = Manifest.permission.CAMERA){
        val selfPermission = ContextCompat.checkSelfPermission(context, permission)
        when {
            selfPermission == PackageManager.PERMISSION_GRANTED -> {
                // Granted
            }
            shouldShowRequestPermissionRationale(permission) -> {
                // Confused
                //showRationale(...)
            }
            else -> {
                // Denied/PermanentlyDenied
                //request()
            }
        }
    }

    private fun requestPermission(permission: String){
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                // Granted
            } else {
                if (shouldShowRequestPermissionRationale(permission)) {
                    // Denied
                } else {
                    // PermanentlyDenied
                }
            }
        }
    }




    @Preview(showBackground = true)
    @Composable
    fun PermissionsPreview() {
        PermissionsScreen()
    }
}
