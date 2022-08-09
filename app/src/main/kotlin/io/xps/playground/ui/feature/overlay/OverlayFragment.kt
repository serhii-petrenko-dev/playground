package io.xps.playground.ui.feature.overlay

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import io.xps.playground.R
import io.xps.playground.databinding.FragmentComposeBinding
import io.xps.playground.tools.viewBinding
import io.xps.playground.ui.theme.PlaygroundTheme
import kotlin.random.Random

@AndroidEntryPoint
class OverlayFragment: Fragment(R.layout.fragment_compose) {

    private val binding by viewBinding(FragmentComposeBinding::bind)

    private lateinit var overlay: OverlayView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.containerCompose.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        binding.containerCompose.setContent {
            PlaygroundTheme {
                OverlayScreen()
            }
        }
        overlay = OverlayView(binding.root.context)
    }

    @Composable
    fun OverlayScreen() {
        Surface(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .padding(end = 16.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.End
            ) {
                Button(
                    onClick = { overlay.attach() },
                    content = { Text(text = "Attach") }
                )
                Button(
                    onClick = { overlay.displayBubble(Random.nextInt().toString()) },
                    content = { Text(text = "Post notification") }
                )
            }
        }
    }
}
