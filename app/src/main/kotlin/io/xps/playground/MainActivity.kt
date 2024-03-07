package io.xps.playground

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.android.AndroidEntryPoint
import io.xps.playground.databinding.ActivityMainBinding
import io.xps.playground.tools.NavigationDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

const val TRANSLUCENT_STATUS = 67108864

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var navigationDispatcher: NavigationDispatcher

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initNavigation()
    }

    private fun applyEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.clearFlags(TRANSLUCENT_STATUS)
        window.statusBarColor = Color.Transparent.toArgb()
        window.navigationBarColor = Color.Transparent.toArgb()
    }

    private fun initNavigation() {
        val host = supportFragmentManager.findFragmentById(R.id.fragmentHost) as NavHostFragment
        val navInflater = host.navController.navInflater
        val navGraph = navInflater.inflate(R.navigation.main_graph)
        host.navController.graph = navGraph
        navController = host.navController
        navController.addOnDestinationChangedListener(changeListener)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                observeNavigationCommands()
            }
        }
    }

    private suspend fun observeNavigationCommands() {
        for (command in navigationDispatcher.navigationEmitter) {
            command.invoke(Navigation.findNavController(this@MainActivity, R.id.fragmentHost))
        }
    }

    private val changeListener = NavController.OnDestinationChangedListener { _, destination, _ ->
        when (destination.id) {
            else -> {
                // Do something
            }
        }
    }
}
