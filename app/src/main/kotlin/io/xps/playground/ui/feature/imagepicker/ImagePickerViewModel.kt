package io.xps.playground.ui.feature.imagepicker

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.xps.playground.domain.ContentRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImagePickerViewModel @Inject constructor(private val repo: ContentRepo) : ViewModel() {

    private val _persistMediaAccess = MutableStateFlow(false)
    val persistMediaAccess = _persistMediaAccess.asStateFlow()

    val imageUri = repo.readUri()

    fun storeUri(uri: Uri?) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.storeUri(uri)
        }
    }

    fun mediaAccess(persist: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            _persistMediaAccess.update { persist }
        }
    }

}
