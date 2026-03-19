package net.brightroom.uniso.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class AppColorScheme(
    val backgroundPrimary: Color,
    val backgroundSecondary: Color,
    val backgroundTertiary: Color,
    val backgroundInfo: Color,
    val backgroundWarning: Color,
    val backgroundDanger: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val textInfo: Color,
    val textWarning: Color,
    val textDanger: Color,
    val borderPrimary: Color,
    val borderSecondary: Color,
    val borderTertiary: Color,
)

val LightAppColorScheme =
    AppColorScheme(
        backgroundPrimary = LightBackgroundPrimary,
        backgroundSecondary = LightBackgroundSecondary,
        backgroundTertiary = LightBackgroundTertiary,
        backgroundInfo = LightBackgroundInfo,
        backgroundWarning = LightBackgroundWarning,
        backgroundDanger = LightBackgroundDanger,
        textPrimary = LightTextPrimary,
        textSecondary = LightTextSecondary,
        textTertiary = LightTextTertiary,
        textInfo = LightTextInfo,
        textWarning = LightTextWarning,
        textDanger = LightTextDanger,
        borderPrimary = LightBorderPrimary,
        borderSecondary = LightBorderSecondary,
        borderTertiary = LightBorderTertiary,
    )

val DarkAppColorScheme =
    AppColorScheme(
        backgroundPrimary = DarkBackgroundPrimary,
        backgroundSecondary = DarkBackgroundSecondary,
        backgroundTertiary = DarkBackgroundTertiary,
        backgroundInfo = DarkBackgroundInfo,
        backgroundWarning = DarkBackgroundWarning,
        backgroundDanger = DarkBackgroundDanger,
        textPrimary = DarkTextPrimary,
        textSecondary = DarkTextSecondary,
        textTertiary = DarkTextTertiary,
        textInfo = DarkTextInfo,
        textWarning = DarkTextWarning,
        textDanger = DarkTextDanger,
        borderPrimary = DarkBorderPrimary,
        borderSecondary = DarkBorderSecondary,
        borderTertiary = DarkBorderTertiary,
    )

val LocalAppColorScheme =
    staticCompositionLocalOf {
        LightAppColorScheme
    }

object AppColors {
    val current: AppColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColorScheme.current
}
