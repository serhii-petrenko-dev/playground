package io.xps.playground.ui.feature.filesystem

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.xps.playground.tools.NavigationDispatcher
import javax.inject.Inject

@HiltViewModel
class FileSystemViewModel @Inject constructor(
    private val navigationDispatcher: NavigationDispatcher
) : ViewModel() {

    var sampleDataSize = 200f

}
