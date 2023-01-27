package io.xps.playground.ui.feature.main

import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes
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

        val element = Destination(id = R.id.goPermissions, name = R.string.permissions, drawableRes = R.drawable.ic_hand)
        destinations.add(element)
        // destinations.add(Destination(id = R.id.goPermissions, name = R.string.compose, drawableRes = R.drawable.ic_hexagon, parent = element))
        // destinations.add(Destination(id = R.id.goPermissions, name = R.string.view, drawableRes = R.drawable.ic_android, parent = element))

        destinations.add(Destination(id = R.id.goOverlay, name = R.string.floating_overlay, drawableRes = R.drawable.ic_overlay))
        destinations.add(Destination(id = R.id.goMultiCam, name = R.string.multiple_cameras, drawableRes = R.drawable.ic_camera_multi))
        destinations.add(Destination(id = R.id.goInputsDemo, name = R.string.inputs_demo, drawableRes = R.drawable.ic_text_fields))
        destinations.add(Destination(id = R.id.goFileSystem, name = R.string.file_system, drawableRes = R.drawable.ic_folder))
        destinations.add(Destination(id = R.id.goWorkManager, name = R.string.work_manager, drawableRes = R.drawable.ic_downloading))
        destinations.add(Destination(id = R.id.goLanguagePicker, name = R.string.language_picker, drawableRes = R.drawable.ic_translate))
        destinations.add(Destination(id = R.id.goImagePicker, name = R.string.image_picker, drawableRes = R.drawable.ic_image_search))

        return destinations
    }

    data class Destination(
        @IdRes val id: Int?,
        @StringRes
        val name: Int,
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
