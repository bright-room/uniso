package net.brightroom.uniso

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import net.brightroom.uniso.di.AppDependencies
import net.brightroom.uniso.platform.JvmKeychainAccessor
import net.brightroom.uniso.platform.JvmPlatformLocale
import net.brightroom.uniso.platform.JvmPlatformPaths
import net.brightroom.uniso.ui.LocalI18n
import net.brightroom.uniso.ui.MainLayout
import net.brightroom.uniso.ui.sidebar.SidebarViewModel
import net.brightroom.uniso.ui.sidebar.WebViewLifecycleCallback
import net.brightroom.uniso.ui.theme.AppTheme
import net.brightroom.uniso.ui.webview.CefInitState
import net.brightroom.uniso.ui.webview.SplashScreen
import net.brightroom.uniso.ui.webview.WebViewPanel

fun main() {
    val dependencies =
        AppDependencies(
            platformPaths = JvmPlatformPaths(),
            keychainAccessor = JvmKeychainAccessor(),
            platformLocale = JvmPlatformLocale(),
        )
    dependencies.initialize()

    application {
        val scope = rememberCoroutineScope()
        val cefState by dependencies.cefInitializer.initState.collectAsState()
        val webViewLifecycleManager = dependencies.webViewLifecycleManager
        val sidebarViewModel =
            remember {
                SidebarViewModel(
                    accountManager = dependencies.accountManager,
                    servicePluginRegistry = dependencies.servicePluginRegistry,
                    webViewLifecycleCallback =
                        object : WebViewLifecycleCallback {
                            override fun onAccountDeleted(accountId: String) {
                                webViewLifecycleManager.destroyWebView(accountId)
                            }
                        },
                    scope = scope,
                )
            }

        LaunchedEffect(Unit) {
            dependencies.cefInitializer.initialize()
        }

        // Activate WebViews for the active account when it changes
        val activeAccountId by dependencies.accountManager.activeAccountId.collectAsState()
        LaunchedEffect(activeAccountId) {
            activeAccountId?.let { webViewLifecycleManager.activateWebView(it) }
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
                CompositionLocalProvider(LocalI18n provides dependencies.i18nManager) {
                    val webViewReady = cefState is CefInitState.Ready
                    val sidebarAccounts by sidebarViewModel.sidebarAccounts.collectAsState()
                    val activatedIds by webViewLifecycleManager.activatedAccountIds.collectAsState()
                    val activatedAccounts = sidebarAccounts.filter { it.accountId in activatedIds }

                    if (webViewReady) {
                        MainLayout(
                            viewModel = sidebarViewModel,
                            activatedAccounts = activatedAccounts,
                            webViewReady = true,
                            webViewContent = { accounts, activeId, visible ->
                                WebViewPanel(
                                    accounts = accounts,
                                    activeAccountId = activeId,
                                    visible = visible,
                                )
                            },
                        )
                    } else {
                        SplashScreen(initState = cefState)
                    }
                }
            }
        }
    }
}
