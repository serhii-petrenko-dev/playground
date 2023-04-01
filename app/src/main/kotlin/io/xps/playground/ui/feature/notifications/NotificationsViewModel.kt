package io.xps.playground.ui.feature.notifications

import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.xps.playground.PERMISSION_KEY
import io.xps.playground.R
import io.xps.playground.tools.NavigationDispatcher
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val navigationDispatcher: NavigationDispatcher
) : ViewModel() {

    fun askPermission(permission: String) {
        navigationDispatcher.emit {
            it.navigate(
                R.id.goPermissions,
                bundleOf(PERMISSION_KEY to permission)
            )
        }
    }
}
