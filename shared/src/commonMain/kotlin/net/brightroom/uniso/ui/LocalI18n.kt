package net.brightroom.uniso.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import net.brightroom.uniso.domain.settings.I18nManager
import net.brightroom.uniso.domain.settings.StringKey

val LocalI18n =
    staticCompositionLocalOf<I18nManager> {
        error("I18nManager not provided")
    }

@Composable
@ReadOnlyComposable
fun stringResource(key: StringKey): String = LocalI18n.current.getString(key)
