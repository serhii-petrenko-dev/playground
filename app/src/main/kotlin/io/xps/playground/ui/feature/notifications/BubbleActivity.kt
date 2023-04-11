package io.xps.playground.ui.feature.notifications

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import io.xps.playground.databinding.FragmentComposeBinding

@AndroidEntryPoint
class BubbleActivity : AppCompatActivity() {

    private lateinit var binding: FragmentComposeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentComposeBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
