package io.xps.playground.ui.feature.workManager

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemGesturesPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import io.xps.playground.R
import io.xps.playground.databinding.FragmentComposeBinding
import io.xps.playground.tools.viewBinding
import io.xps.playground.ui.composables.ScreenTittle
import io.xps.playground.ui.feature.workManager.worker.KEY_IMAGE_URI
import io.xps.playground.ui.theme.PlaygroundTheme

@AndroidEntryPoint
class WorkManagerFragment : Fragment(R.layout.fragment_compose) {

    private val binding by viewBinding(FragmentComposeBinding::bind)
    private val viewModel by viewModels<WorkManagerViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.containerCompose.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        binding.containerCompose.setContent {
            PlaygroundTheme {
                WorkManagerScreen(viewModel)
            }
        }

        viewModel.workInfo.observe(viewLifecycleOwner) {
            it?.firstOrNull()?.let { info ->
                val outputUri = info.outputData.getString(KEY_IMAGE_URI)
                viewModel.workStatus(!info.state.isFinished, outputUri)
            }
        }
    }

    @Composable
    fun WorkManagerScreen(viewModel: WorkManagerViewModel) {
        Column {
            ScreenTittle(text = "Work Manager")
            val blurAmount by viewModel.blurAmount.collectAsState()
            val workRunning by viewModel.workRunning.collectAsState()
            val outputUri by viewModel.outputUri.collectAsState()
            Image(
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 16.dp
                ),
                painter = painterResource(id = R.drawable.android),
                contentDescription = null
            )

            Slider(
                modifier = Modifier.systemGesturesPadding(),
                enabled = !workRunning,
                value = blurAmount,
                onValueChange = viewModel::blurChange,
                valueRange = 0f..2f,
                steps = 1
            )

            if (outputUri != null) {
                val context = LocalContext.current
                Button(
                    modifier = Modifier.align(CenterHorizontally),
                    onClick = {
                        val actionView = Intent(Intent.ACTION_VIEW, outputUri)
                        if (actionView.resolveActivity(context.packageManager) != null) {
                            startActivity(actionView)
                        }
                    },
                    content = { Text(text = "Watch Blured") }
                )
            }
            if (workRunning) {
                Button(
                    modifier = Modifier.align(CenterHorizontally),
                    onClick = viewModel::cancel,
                    content = { Text(text = "Cancel") }
                )
            }
        }
    }
}
