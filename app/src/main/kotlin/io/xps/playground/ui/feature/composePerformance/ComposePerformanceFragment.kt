package io.xps.playground.ui.feature.composePerformance

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import io.xps.playground.R
import io.xps.playground.databinding.FragmentComposeBinding
import io.xps.playground.tools.viewBinding
import io.xps.playground.ui.theme.PlaygroundTheme
import kotlinx.collections.immutable.persistentListOf
import kotlin.random.Random

@AndroidEntryPoint
class ComposePerformanceFragment : Fragment(R.layout.fragment_compose) {

    private val binding by viewBinding(FragmentComposeBinding::bind)
    private val viewModel by viewModels<ComposePerformanceViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.containerCompose.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        binding.containerCompose.setContent {
            PlaygroundTheme {
                Column(Modifier.systemBarsPadding()) {
                    UnstableObjectsSample()
                    UnstableLambdaUsageSample1()
                    UnstableLambdaUsageSample2(viewModel)
                    InlinedComposableSample1()
                    InlinedComposableSample2()
                    LambdaModifierSample()
                }
            }
        }
    }

    @Composable
    fun UnstableObjectsSample() {
        val contact by remember {
            mutableStateOf(
                Contact(
                    id = Random.nextLong(),
                    isLoading = false,
                    names = persistentListOf("John", "Jane", "Jake")
                )
            )
        }

        var selected by remember { mutableStateOf(false) }
        Column(Modifier.systemBarsPadding()) {
            Checkbox(
                checked = selected,
                onCheckedChange = { selected = it }
            )
            ContactList(contact)
        }
    }

    @Composable
    fun ContactList(contact: Contact) {
        Box(contentAlignment = Alignment.Center) {
            if (contact.isLoading) {
                CircularProgressIndicator()
            } else {
                Text(contact.names.toString())
            }
        }
    }

    @Composable
    fun UnstableLambdaUsageSample1() {
        var selected by remember { mutableStateOf(false) }
        val focusRequester = LocalFocusManager.current
        val clearFocus = remember { { focusRequester.clearFocus() } } // stable lambda

        Column(Modifier.systemBarsPadding()) {
            Checkbox(
                checked = selected,
                onCheckedChange = { selected = it }
            )
            Button(
                // onClick = { focusRequester.clearFocus() }, // unstable lambda
                onClick = { clearFocus() } // stable lambda
            ) {
                Text(text = "button with lambda")
            }
        }
    }

    @Composable
    fun UnstableLambdaUsageSample2(viewModel: ComposePerformanceViewModel) {
        var selected by remember { mutableStateOf(false) }

        Column(modifier = Modifier.systemBarsPadding()) {
            Checkbox(
                checked = selected,
                onCheckedChange = { selected = it }
            )

            val onClick = remember { viewModel::onClicked } // stable lambda
            Button(onClick = onClick) {
                Text(text = "button with unstable lambda")
            }
        }
    }

    @Composable
    fun InlinedComposableSample1() {
        var count by remember { mutableStateOf(0) }

        Column {
            Text(text = "count: $count")
            Button(onClick = { count++ }) {
                Text(text = "count++")
            }
        }
    }

    @Composable
    fun InlinedComposableSample2() {
        var count by remember { mutableStateOf(0) }

        WrappedColumn {
            Text(text = "count: $count")
            Button(onClick = { count++ }) {
                Text(text = "count++")
            }
        }
    }

    @Composable
    fun LambdaModifierSample() {
        val scrollState = rememberScrollState()

        WrappedBox(modifier = Modifier.background(Color.Black)) {
            val scrollProvider = remember { { scrollState.value * 1.5f } }
            ScrollingArea(scrollState)
            // ScrollingArea(scrollState.value * 1.5f) // Causes recomposition
            HorizontallyMovingButton(scrollProvider)
        }
    }

    @Composable
    private fun ScrollingArea(scrollState: ScrollState) {
        Spacer(
            modifier = Modifier
                .horizontalScroll(scrollState)
                .width(2000.dp)
                .height(50.dp)
        )
    }

    @Composable
    // private fun HorizontallyMovingButton(scrollOffset: Float) { // Causes recomposition
    private fun HorizontallyMovingButton(scrollProvider: () -> Float) {
        Button(
            // modifier = Modifier.graphicsLayer(translationX = scrollOffset), // Causes recomposition
            modifier = Modifier.graphicsLayer { translationX = scrollProvider() },
            onClick = { },
            content = { }
        )
    }

    @Composable
    fun WrappedBox(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
        Box(modifier = modifier, content = content)
    }

    @Composable
    fun WrappedColumn(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
        Column(modifier = modifier, content = content)
    }
}
