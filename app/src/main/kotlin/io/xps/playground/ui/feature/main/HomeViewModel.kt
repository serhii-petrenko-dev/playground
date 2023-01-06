package io.xps.playground.ui.feature.main

import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
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

    val items = boostrapNavigation()

    private val searchKey = "search"
    val searchQuery = handle.getStateFlow(searchKey, "")

    fun input(query: String){
        handle[searchKey] = query
    }

    fun navigate(destination: Destination){
        destination.id ?: return
        navigationDispatcher.emit {
            it.navigate(destination.id)
        }
    }

    private fun boostrapNavigation(): List<Destination> {
        val destinations = mutableListOf<Destination>()

        val element = Destination(id = R.id.goPermissions, name = "Permissions", drawableRes = R.drawable.ic_hand)
        destinations.add(element)
        destinations.add(Destination(id = R.id.goPermissions, name = "Compose", drawableRes = R.drawable.ic_hexagon, parent = element))
        destinations.add(Destination(id = R.id.goPermissions, name = "View", drawableRes = R.drawable.ic_android, parent = element))

        destinations.add(Destination(id = R.id.goOverlay, name = "Floating Overlay", drawableRes = R.drawable.ic_overlay))
        destinations.add(Destination(id = R.id.goMultiCam, name = "Multiple Cameras", drawableRes = R.drawable.ic_camera_multi))
        destinations.add(Destination(id = R.id.goInputsDemo, name = "Inputs Demo", drawableRes = R.drawable.ic_text_fields))
        destinations.add(Destination(id = R.id.goFileSystem, name = "File System", drawableRes = R.drawable.ic_folder))
        destinations.add(Destination(id = R.id.goWorkManager, name = "Work Manager", drawableRes = R.drawable.ic_downloading))

        return destinations
    }

    data class Destination(
        @IdRes val id: Int?,
        val name: String,
        val hint: String = "",
        @DrawableRes val drawableRes: Int,
        val parent: Destination? = null
    ) {

        val tabBy
        get() = run {
            var tab = 1
            if(parent != null) tab++
            return@run if (tab == 1) tab else tab * 2
        }
    }
}
