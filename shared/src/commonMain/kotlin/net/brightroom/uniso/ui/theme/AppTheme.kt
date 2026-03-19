package net.brightroom.uniso.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val LightColorScheme =
    lightColorScheme(
        background = LightBackgroundPrimary,
        surface = LightBackgroundPrimary,
        surfaceVariant = LightBackgroundSecondary,
        surfaceContainerHighest = LightBackgroundTertiary,
        onBackground = LightTextPrimary,
        onSurface = LightTextPrimary,
        onSurfaceVariant = LightTextSecondary,
        outline = LightBorderSecondary,
        outlineVariant = LightBorderTertiary,
        error = LightTextDanger,
        errorContainer = LightBackgroundDanger,
        onError = LightBackgroundPrimary,
        onErrorContainer = LightTextDanger,
        primary = LightTextPrimary,
        onPrimary = LightBackgroundPrimary,
        tertiary = LightTextInfo,
        tertiaryContainer = LightBackgroundInfo,
        onTertiaryContainer = LightTextInfo,
    )

private val DarkColorScheme =
    darkColorScheme(
        background = DarkBackgroundPrimary,
        surface = DarkBackgroundPrimary,
        surfaceVariant = DarkBackgroundSecondary,
        surfaceContainerHighest = DarkBackgroundTertiary,
        onBackground = DarkTextPrimary,
        onSurface = DarkTextPrimary,
        onSurfaceVariant = DarkTextSecondary,
        outline = DarkBorderSecondary,
        outlineVariant = DarkBorderTertiary,
        error = DarkTextDanger,
        errorContainer = DarkBackgroundDanger,
        onError = DarkBackgroundPrimary,
        onErrorContainer = DarkTextDanger,
        primary = DarkTextPrimary,
        onPrimary = DarkBackgroundPrimary,
        tertiary = DarkTextInfo,
        tertiaryContainer = DarkBackgroundInfo,
        onTertiaryContainer = DarkTextInfo,
    )

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme =
        if (darkTheme) {
            DarkColorScheme
        } else {
            LightColorScheme
        }

    val appColorScheme =
        if (darkTheme) {
            DarkAppColorScheme
        } else {
            LightAppColorScheme
        }

    CompositionLocalProvider(LocalAppColorScheme provides appColorScheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content,
        )
    }
}
