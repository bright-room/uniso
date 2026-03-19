package net.brightroom.uniso

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import net.brightroom.uniso.ui.MainLayout
import net.brightroom.uniso.ui.theme.AppTheme

fun main() =
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "${Constants.APP_NAME} — ${getPlatformName()}",
            state = rememberWindowState(width = 1280.dp, height = 800.dp),
        ) {
            AppTheme {
                MainLayout()
            }
        }
    }
