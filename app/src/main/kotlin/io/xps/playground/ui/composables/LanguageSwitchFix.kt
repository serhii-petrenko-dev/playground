package io.xps.playground.ui.composables

import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.text.layoutDirection
import io.xps.playground.ui.feature.languagePicker.LANGUAGES

@Composable // https://issuetracker.google.com/issues/236538894
fun FixInAppLanguageSwitchLayoutDirection(content: @Composable () -> Unit) {
    val appLocale = AppCompatDelegate
        .getApplicationLocales()
        .getFirstMatch(
            LANGUAGES
                .map { it.locale }
                .toTypedArray()
        ) ?: LocalConfiguration.current.locales[0]
    val appLocaleDirection = when (appLocale.layoutDirection) {
        View.LAYOUT_DIRECTION_RTL -> LayoutDirection.Rtl
        else -> LayoutDirection.Ltr
    }
    CompositionLocalProvider(LocalLayoutDirection provides appLocaleDirection) {
        content()
    }
}
