package io.xps.playground.ui.feature.main

import android.os.Bundle
import android.view.View
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import io.xps.playground.R
import io.xps.playground.databinding.FragmentComposeBinding
import io.xps.playground.tools.keyboardAsState
import io.xps.playground.tools.viewBinding
import io.xps.playground.ui.composables.ScreenTittle
import io.xps.playground.ui.feature.main.HomeViewModel.Destination
import io.xps.playground.ui.theme.PlaygroundTheme
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment: Fragment(R.layout.fragment_compose) {

    private val binding by viewBinding(FragmentComposeBinding::bind)
    private val viewModel by viewModels<HomeViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.containerCompose.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        binding.containerCompose.setContent {
            PlaygroundTheme {
                val items = remember { viewModel.items }
                val searchQuery by viewModel.searchQuery.collectAsState()
                HomeScreen(
                    searchQuery = searchQuery,
                    items = items,
                    onClick = viewModel::navigate,
                    onSearchQuery = viewModel::input
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun HomeScreen(
        searchQuery: String,
        items: List<Destination>,
        onClick: (Destination) -> Unit,
        onSearchQuery: (String) -> Unit
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { contentPadding ->
            Surface(modifier = Modifier.padding(bottom = contentPadding.calculateBottomPadding())) {
                val keyboardIsOpen = keyboardAsState()
                val scrollState = rememberLazyListState()
                val scope = rememberCoroutineScope()
                LazyColumn(
                    modifier = Modifier.imePadding(),
                    state = scrollState
                ) {
                    item {
                        AnimatedVisibility(
                            visible = !keyboardIsOpen.value,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            if(!keyboardIsOpen.value) {
                                LaunchedEffect(scope){
                                    scope.launch {
                                        scrollState.animateScrollToItem(0)
                                    }
                                }
                            }
                            ScreenTittle(text = "Playground")
                        }
                    }
                    item {
                        TextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(16.dp)
                                .clip(RoundedCornerShape(50)),
                            value = searchQuery,
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodySmall,
                            leadingIcon = {
                                Icon(
                                    modifier = Modifier.padding(start = 24.dp, end = 8.dp),
                                    painter = painterResource(id = R.drawable.ic_search),
                                    contentDescription = null
                                )
                            },
                            placeholder = {
                                Text(
                                    text = "Search",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            colors = TextFieldDefaults.textFieldColors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent
                            ),
                            onValueChange = onSearchQuery
                        )
                    }
                    items(items) {
                        DestinationItem(it, onClick)
                    }
                }
            }
        }
    }

    @Composable
    fun DestinationItem(destination: Destination, onClick: (Destination) -> Unit){
        Row(
            modifier = Modifier
                .clickable { onClick(destination) }
                .fillMaxWidth()
                .padding(
                    start = (destination.tabBy * 16).dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 16.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.padding(end = 16.dp),
                painter = painterResource(id = destination.drawableRes),
                contentDescription = null
            )
           Column(
               verticalArrangement = Arrangement.Center
           ) {
               Text(
                   text = stringResource(id = destination.name),
                   style = MaterialTheme.typography.bodyMedium,
               )
               if(destination.hint.isNotBlank()){
                   Text(
                       modifier = Modifier.alpha(0.7f),
                       text = stringResource(id = destination.name),
                       style = MaterialTheme.typography.bodySmall
                   )
               }
           }
        }
    }
}
