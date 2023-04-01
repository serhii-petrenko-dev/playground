package io.xps.playground.ui.feature.inputs

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.xps.playground.tools.NavigationDispatcher
import javax.inject.Inject

@HiltViewModel
class InputsViewModel @Inject constructor(
    private val navigationDispatcher: NavigationDispatcher,
    private val handle: SavedStateHandle
) : ViewModel() {

    private val transformationKey = "transformation"
    val textWithTransformation = handle.getStateFlow(transformationKey, "12312")

    fun input(textWithTransformation: String) {
        handle[transformationKey] = textWithTransformation
    }
}
