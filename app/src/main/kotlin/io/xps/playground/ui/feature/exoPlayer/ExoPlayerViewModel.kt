package io.xps.playground.ui.feature.exoPlayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.xps.playground.data.FakeData
import io.xps.playground.domain.VideoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExoPlayerViewModel @Inject constructor() : ViewModel() {

    private val _events = Channel<ScreenEvent>()
    val events = _events.receiveAsFlow()

    private val _videos = MutableStateFlow<List<VideoItem>>(listOf())
    val videos = _videos.asStateFlow()

    init {
        _videos.value = FakeData.videos()
    }

    fun next() {
        viewModelScope.launch(Dispatchers.IO) {
            _events.send(ScreenEvent.Next)
        }
    }

    fun previous() {
        viewModelScope.launch(Dispatchers.IO) {
            _events.send(ScreenEvent.Previous)
        }
    }

    sealed class ScreenEvent {
        object Previous : ScreenEvent()
        object Next : ScreenEvent()
    }
}
