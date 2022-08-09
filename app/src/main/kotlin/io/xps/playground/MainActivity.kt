package io.xps.playground

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.android.AndroidEntryPoint
import io.xps.playground.databinding.ActivityMainBinding
import io.xps.playground.tools.NavigationDispatcher
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var navigationDispatcher: NavigationDispatcher

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        applyEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        initNavigation()

        lifecycleScope.launchWhenResumed { observeNavigationCommands() }
    }

    private fun applyEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    private fun initNavigation() {
        (supportFragmentManager.findFragmentById(R.id.fragmentHost) as NavHostFragment).also { navHost ->
            val navInflater = navHost.navController.navInflater
            val navGraph = navInflater.inflate(R.navigation.main_graph)
            navHost.navController.graph = navGraph
            navController = navHost.navController
            navController.addOnDestinationChangedListener(destinationChangedListener)
        }
    }

    private suspend fun observeNavigationCommands() {
        for (command in navigationDispatcher.navigationEmitter) {
            command.invoke(Navigation.findNavController(this@MainActivity, R.id.fragmentHost))
        }
    }

    private val destinationChangedListener = NavController.OnDestinationChangedListener { _, destination, _ ->
        when(destination.id) {
            else -> { /* Do something */ }
        }
    }
}
