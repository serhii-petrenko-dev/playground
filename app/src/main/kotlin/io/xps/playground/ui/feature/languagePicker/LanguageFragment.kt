package io.xps.playground.ui.feature.languagePicker

import android.os.Bundle
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate.getApplicationLocales
import androidx.appcompat.app.AppCompatDelegate.setApplicationLocales
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.Start
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import io.xps.playground.R
import io.xps.playground.databinding.FragmentComposeBinding
import io.xps.playground.tools.viewBinding
import io.xps.playground.ui.composables.BaseColumn
import io.xps.playground.ui.composables.FixInAppLanguageSwitchLayoutDirection
import io.xps.playground.ui.composables.ListItem
import io.xps.playground.ui.composables.ScreenTittle
import io.xps.playground.ui.theme.PlaygroundTheme
import java.util.Locale

val LANGUAGES: List<LanguageItem> = listOf(
    LanguageItem(R.string.system_default, R.drawable.ic_text_fields, ""),
    LanguageItem(R.string.eng, R.drawable.ic_translate, "en"),
    LanguageItem(R.string.ar, R.drawable.ic_translate, "ar")
)

@AndroidEntryPoint
class LanguageFragment : Fragment(R.layout.fragment_compose) {

    private val binding by viewBinding(FragmentComposeBinding::bind)

    private val currentLocale = mutableStateOf(getCurrentLocale())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.containerCompose.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )

        binding.containerCompose.setContent {
            PlaygroundTheme {
                val languages = remember { LANGUAGES }
                BaseScreen(
                    languages = languages,
                    currentLocale = currentLocale.value,
                    onClick = { language ->
                        val localeList = LocaleListCompat.create(Locale(language.locale))
                        setApplicationLocales(localeList)
                    }
                )
            }
        }
    }

    @Composable
    fun BaseScreen(
        currentLocale: Locale?,
        languages: List<LanguageItem>,
        onClick: (LanguageItem) -> Unit
    ) = FixInAppLanguageSwitchLayoutDirection {
        Surface {
            BaseColumn {
                ScreenTittle(
                    modifier = Modifier
                        .align(Start)
                        .padding(bottom = 4.dp),
                    text = stringResource(id = R.string.language_picker)
                )

                LazyColumn(modifier = Modifier.imePadding()) {
                    items(languages) {
                        ListItem(
                            tittle = stringResource(id = it.tittle),
                            hint = it.locale,
                            drawable = it.drawable,
                            isSelected = currentLocale?.toLanguageTag().equals(it.locale),
                            onClick = { onClick(it) }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        currentLocale.value = getCurrentLocale()
    }

    private fun getCurrentLocale(): Locale? {
        val currentLanguageTag = getApplicationLocales().toLanguageTags()
        return when {
            currentLanguageTag.isEmpty() -> null
            else -> Locale.forLanguageTag(currentLanguageTag)
        }
    }
}

data class LanguageItem(
    @StringRes val tittle: Int,
    @DrawableRes val drawable: Int,
    val locale: String
)
