package io.xps.playground.ui.feature.baseview

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import io.xps.playground.R
import io.xps.playground.databinding.FragmentComposeBinding
import io.xps.playground.tools.viewBinding

@AndroidEntryPoint
class BaseFragment: Fragment(R.layout.fragment_compose) {

    private val binding by viewBinding(FragmentComposeBinding::bind)
    private val viewModel by viewModels<BaseViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

}
