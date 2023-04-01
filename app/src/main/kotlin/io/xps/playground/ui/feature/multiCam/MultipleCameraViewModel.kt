package io.xps.playground.ui.feature.multiCam

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.xps.playground.tools.NavigationDispatcher
import javax.inject.Inject

@HiltViewModel
class MultipleCameraViewModel @Inject constructor(
    private val navigationDispatcher: NavigationDispatcher
) : ViewModel()
