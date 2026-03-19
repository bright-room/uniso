package net.brightroom.uniso

import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import net.brightroom.uniso.di.AppDependencies
import net.brightroom.uniso.platform.JvmKeychainAccessor
import net.brightroom.uniso.platform.JvmPlatformPaths
import net.brightroom.uniso.ui.MainLayout
import net.brightroom.uniso.ui.sidebar.SidebarViewModel
import net.brightroom.uniso.ui.theme.AppTheme

fun main() {
    val dependencies =
        AppDependencies(
            platformPaths = JvmPlatformPaths(),
            keychainAccessor = JvmKeychainAccessor(),
        )
    dependencies.initialize()

    application {
        val scope = rememberCoroutineScope()
        val sidebarViewModel =
            remember {
                SidebarViewModel(
                    accountManager = dependencies.accountManager,
                    servicePluginRegistry = dependencies.servicePluginRegistry,
                    scope = scope,
                )
            }

        Window(
            onCloseRequest = {
                dependencies.close()
                exitApplication()
            },
            title = "${Constants.APP_NAME} — ${getPlatformName()}",
            state = rememberWindowState(width = 1280.dp, height = 800.dp),
        ) {
            AppTheme {
                MainLayout(viewModel = sidebarViewModel)
            }
        }
    }
}
