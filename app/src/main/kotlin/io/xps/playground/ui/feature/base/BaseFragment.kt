package io.xps.playground.ui.feature.base

import android.os.Bundle
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import io.xps.playground.R
import io.xps.playground.databinding.FragmentComposeBinding
import io.xps.playground.tools.viewBinding
import io.xps.playground.ui.composables.BaseColumn
import io.xps.playground.ui.theme.PlaygroundTheme

@AndroidEntryPoint
class BaseFragment: Fragment(R.layout.fragment_compose) {

    private val binding by viewBinding(FragmentComposeBinding::bind)
    private val viewModel by viewModels<BaseViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.containerCompose.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        binding.containerCompose.setContent {
            PlaygroundTheme {
                BaseScreen()
            }
        }
    }

    @Composable
    fun BaseScreen() {
        BaseColumn {

        }
    }

    @Preview
    @Composable
    fun Preview(){
        BaseScreen()
    }
}
