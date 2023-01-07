package io.xps.playground.ui.feature.languagepicker

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate.getApplicationLocales
import androidx.appcompat.app.AppCompatDelegate.setApplicationLocales
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import io.xps.playground.R
import io.xps.playground.databinding.FragmentComposeBinding
import io.xps.playground.tools.viewBinding
import io.xps.playground.ui.composables.BaseColumn
import io.xps.playground.ui.theme.PlaygroundTheme
import java.util.*

@AndroidEntryPoint
class LanguageFragment: Fragment(R.layout.fragment_compose) {

    private val binding by viewBinding(FragmentComposeBinding::bind)

    private var isEngLocale by mutableStateOf(true)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.containerCompose.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        binding.containerCompose.setContent {
            PlaygroundTheme {
                BaseScreen(
                    isEngLocale = isEngLocale,
                    onClick = { useEng ->
                        if (isEngLocale != useEng){
                            val locale = if (useEng) {
                                Locale("en", "US")
                            } else Locale("uk", "UA")
                            val localeList = LocaleListCompat.create(locale)
                            setApplicationLocales(localeList)
                        }
                    }
                )
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun isEng(): Boolean {
        val locales = getApplicationLocales()
        return if (locales.isEmpty) {
            val context = binding.root.context
            val locale: Locale? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                context.resources.configuration.locales.get(0)
            } else context.resources.configuration.locale
            locale?.language == "en"
        } else locales.get(0)?.language == "en"
    }

    @Composable
    fun BaseScreen(isEngLocale: Boolean, onClick: (Boolean) -> Unit) {
        BaseColumn {
            Row(Modifier.fillMaxSize()) {
                Button(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f),
                    shape = RectangleShape,
                    onClick = { onClick(false) },
                    content = {
                        var text = stringResource(id = R.string.ukr)
                        if (!isEngLocale) text += " ✔️"
                        Text(text = text)
                    }
                )
                Button(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f),
                    shape = RectangleShape,
                    onClick = { onClick(true) },
                    content = {
                        var text = stringResource(id = R.string.eng)
                        if (isEngLocale) text += " ✔️"
                        Text(text = text)
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        isEngLocale = isEng()
    }
}
