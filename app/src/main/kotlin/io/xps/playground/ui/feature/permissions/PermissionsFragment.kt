package io.xps.playground.ui.feature.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
        Surface {
            BaseColumn(
                modifier = Modifier.systemBarsPadding(),
                verticalArrangement = Arrangement.Center
            ) {
                val borderColor = MaterialTheme.colorScheme.primary
                val borderModifier = remember {
                   Modifier.padding(16.dp)
                   .border(
                       width = 4.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(12)
                    ).padding(16.dp)
                }

                val cameraPermission = remember { Manifest.permission.CAMERA }
                val cameraPermissionState = rememberPermissionState(cameraPermission)
                if (cameraPermissionState.status == PermissionStatus.Granted) {
                    ContentText(borderModifier, "$cameraPermission permission Granted")
                } else {
                    PermissionDenied(borderModifier, cameraPermission , cameraPermissionState)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val notifyPermission = remember { Manifest.permission.POST_NOTIFICATIONS }
                    val notificationsPermissionState = rememberPermissionState(notifyPermission)
                    if (notificationsPermissionState.status == PermissionStatus.Granted) {
                        ContentText(borderModifier, "$notifyPermission permission Granted")
                    } else {
                        PermissionDenied(
                            borderModifier,
                            notifyPermission,
                            notificationsPermissionState
                        )
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    private fun PermissionDenied(
        modifier: Modifier,
        permission: String,
        permissionState: PermissionState
    ){
        val permissionStatus = permissionState.status
        val textToShow = if (permissionStatus.shouldShowRationale) {
            // If the user has denied the permission but the rationale can be shown,
            // then gently explain why the app requires this permission
            "The permissionState is important for this app. Please grant the permission."
        } else {
            // If it's the first time the user lands on this feature, or the user
            // doesn't want to be asked again for this permission, explain that the
            // permission is required
            "$permission permission required for this feature to be available. " +
                    "Please grant the permission"
        }
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ){
            ContentText(textToShow = textToShow)
            Button(
                onClick = { permissionState.launchPermissionRequest() },
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
    private fun ContentText(modifier: Modifier = Modifier, textToShow: String){
        Text(
            modifier = modifier.fillMaxWidth(0.7f),
            text = textToShow,
            style = MaterialTheme.typography.bodySmall,
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
                    /* Confused
                        showRationale(...)
                    */
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
