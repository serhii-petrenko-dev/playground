package io.xps.playground.ui.feature.main

import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BackHand
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.SettingsOverscan
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.xps.playground.R
import io.xps.playground.tools.NavigationDispatcher
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val navigationDispatcher: NavigationDispatcher,
    private val handle: SavedStateHandle
) : ViewModel() {

    val items by lazy { boostrapNavigation() }

    private val searchKey = "search"
    val searchQuery = handle.getStateFlow(searchKey, "")

    fun input(query: String) {
        handle[searchKey] = query
    }

    fun navigate(destination: Destination) {
        destination.id ?: return
        navigationDispatcher.emit {
            it.navigate(destination.id)
        }
    }

    private fun boostrapNavigation(): List<Destination> {
        val destinations = mutableListOf<Destination>()

        destinations.add(
            Destination(
                id = R.id.goComposePerformance,
                name = R.string.compose_performance,
                icon = Icons.Default.Speed
            )
        )

        destinations.add(
            Destination(
                id = R.id.goPermissions,
                name = R.string.permissions,
                icon = Icons.Default.BackHand
            )
        )
        destinations.add(
            Destination(
                id = R.id.goOverlay,
                name = R.string.floating_overlay,
                icon = Icons.Default.SettingsOverscan
            )
        )
        destinations.add(
            Destination(
                id = R.id.goMultiCam,
                name = R.string.multiple_cameras,
                icon = Icons.Default.Camera
            )
        )
        destinations.add(
            Destination(
                id = R.id.goInputsDemo,
                name = R.string.inputs_demo,
                icon = Icons.Default.TextFields
            )
        )
        destinations.add(
            Destination(
                id = R.id.goFileSystem,
                name = R.string.file_system,
                icon = Icons.Default.Folder
            )
        )
        destinations.add(
            Destination(
                id = R.id.goWorkManager,
                name = R.string.work_manager,
                icon = Icons.Default.Downloading
            )
        )
        destinations.add(
            Destination(
                id = R.id.goLanguagePicker,
                name = R.string.language_picker,
                icon = Icons.Default.Translate
            )
        )
        destinations.add(
            Destination(
                id = R.id.goImagePicker,
                name = R.string.image_picker,
                icon = Icons.Default.ImageSearch
            )
        )
        destinations.add(
            Destination(
                id = R.id.goExoPlayer,
                name = R.string.exo_player,
                icon = Icons.Default.SmartDisplay
            )
        )
        destinations.add(
            Destination(
                id = R.id.goSerialization,
                name = R.string.serialization,
                icon = Icons.Default.Transform
            )
        )
        destinations.add(
            Destination(
                id = R.id.goNotifications,
                name = R.string.notifications,
                icon = Icons.Default.NotificationsActive
            )
        )
        destinations.add(
            Destination(
                id = R.id.goLiquidWidget,
                name = R.string.liquid_widget,
                icon = Icons.Default.WaterDrop
            )
        )

        return destinations
    }

    data class Destination(
        @IdRes val id: Int?,
        @StringRes
        val name: Int,
        val hint: String = "",
        val icon: ImageVector,
        val parent: Destination? = null
    ) {

        val tabBy
            get() = run {
                var tab = 1
                if (parent != null) tab++
                return@run if (tab == 1) tab else tab * 2
            }
    }
}
