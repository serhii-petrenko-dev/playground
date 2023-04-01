package io.xps.playground.ui.feature.exoPlayer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.StyledPlayerView
import dagger.hilt.android.AndroidEntryPoint
import io.xps.playground.R
import io.xps.playground.databinding.FragmentComposeBinding
import io.xps.playground.domain.VideoItem
import io.xps.playground.tools.viewBinding
import io.xps.playground.ui.composables.BaseColumn
import io.xps.playground.ui.theme.PlaygroundTheme

@AndroidEntryPoint
class ExoPlayerFragment : Fragment(R.layout.fragment_compose) {

    private val binding by viewBinding(FragmentComposeBinding::bind)
    private val viewModel by viewModels<ExoPlayerViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.containerCompose.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        binding.containerCompose.setContent {
            val context = LocalContext.current
            val videos by viewModel.videos.collectAsState()
            var activeItem by remember { mutableStateOf(0) }
            val mediaPlayer = remember(context) { MediaPlayer(context) }
            val listState = rememberLazyListState()

            LaunchedEffect(Unit) {
                viewModel.events.collect {
                    when (it) {
                        is ExoPlayerViewModel.ScreenEvent.Previous -> {
                            val index = activeItem - 1
                            if (index >= 0) {
                                listState.animateScrollToItem(index)
                            }
                        }
                        is ExoPlayerViewModel.ScreenEvent.Next -> {
                            val index = activeItem + 1
                            if (index <= videos.lastIndex) {
                                listState.animateScrollToItem(index)
                            }
                        }
                    }
                }
            }

            LaunchedEffect(Unit) {
                snapshotFlow {
                    listState.firstVisibleItemIndex
                }.collect { index ->
                    activeItem = index
                    videos.getOrNull(activeItem)?.let {
                        mediaPlayer.prepare(it.videoUrl)
                    }
                }
            }

            val lifecycle = LocalLifecycleOwner.current.lifecycle
            DisposableEffect(lifecycle) {
                val lifecycleObserver = LifecycleEventObserver { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_RESUME -> {
                            videos.getOrNull(activeItem)?.let {
                                mediaPlayer.prepare(it.videoUrl)
                            }
                        }
                        Lifecycle.Event.ON_PAUSE -> {
                            mediaPlayer.stop()
                        }
                        else -> {}
                    }
                }
                lifecycle.addObserver(lifecycleObserver)

                onDispose {
                    lifecycle.removeObserver(lifecycleObserver)
                }
            }

            PlaygroundTheme {
                ExoPlayer(
                    activeItem = activeItem,
                    player = mediaPlayer.player,
                    videos = videos,
                    previousClick = viewModel::previous,
                    nextClick = viewModel::next,
                    listState = listState
                )
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun ExoPlayer(
        activeItem: Int,
        player: Player,
        videos: List<VideoItem>,
        previousClick: () -> Unit,
        nextClick: () -> Unit,
        listState: LazyListState
    ) {
        Surface {
            BaseColumn(verticalArrangement = Arrangement.Center) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.5f)
                        .padding(bottom = 44.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    state = listState,
                    flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
                ) {
                    itemsIndexed(
                        items = videos,
                        key = { _: Int, item: VideoItem ->
                            item.id
                        }
                    ) { index: Int, _: VideoItem ->
                        if (index == activeItem) {
                            VideoItem(player)
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillParentMaxWidth()
                                    .height(100.dp)
                            )
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        modifier = Modifier
                            .padding(15.dp)
                            .size(72.dp)
                            .clip(CircleShape)
                            .align(Alignment.CenterEnd),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                        contentPadding = PaddingValues(20.dp),
                        onClick = nextClick,
                        content = {
                            Image(
                                modifier = Modifier.fillMaxSize(),
                                painter = painterResource(id = R.drawable.ic_arrow),
                                contentDescription = ""
                            )
                        }
                    )

                    Button(
                        modifier = Modifier
                            .padding(15.dp)
                            .size(72.dp)
                            .clip(CircleShape)
                            .align(Alignment.CenterStart)
                            .rotate(180f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                        contentPadding = PaddingValues(20.dp),
                        onClick = previousClick,
                        content = {
                            Image(
                                modifier = Modifier.fillMaxSize(),
                                painter = painterResource(id = R.drawable.ic_arrow),
                                contentDescription = ""
                            )
                        }
                    )
                }
            }
        }
    }

    @Composable
    fun LazyItemScope.VideoItem(player: Player) {
        val isPlayerUiVisible = remember { mutableStateOf(false) }
        Box(
            modifier = Modifier.fillParentMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            VideoPlayer(player) { uiVisible ->
                if (isPlayerUiVisible.value) {
                    isPlayerUiVisible.value = uiVisible
                } else {
                    isPlayerUiVisible.value = true
                }
            }
        }
    }

    @Composable
    fun VideoPlayer(
        mediaPlayer: Player,
        onControllerVisibilityChanged: (uiVisible: Boolean) -> Unit
    ) {
        val context = LocalContext.current
        val playerView = remember {
            val layout = LayoutInflater.from(context).inflate(
                R.layout.video_player,
                binding.root,
                false
            )
            val playerView = layout.findViewById(R.id.playerView) as StyledPlayerView
            playerView.apply {
                setControllerVisibilityListener(
                    StyledPlayerView.ControllerVisibilityListener { visibility ->
                        onControllerVisibilityChanged(visibility == View.VISIBLE)
                    }
                )
            }
        }

        playerView.player = mediaPlayer

        AndroidView(
            { playerView },
            Modifier
                .height(256.dp)
                .background(Color.Black)
        )
    }
}
