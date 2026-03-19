package net.brightroom.uniso

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import net.brightroom.uniso.ui.dialogs.CrashRecoveryDialog
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

    // Check crash recovery state before entering composition
    val sessionManager = dependencies.sessionManager
    val needsCrashRecovery = !sessionManager.isCleanShutdown()
    val restoredSession =
        if (needsCrashRecovery) {
            sessionManager.restoreSession()
        } else {
            null
        }

    // Mark startup (sets clean_shutdown = 0)
    sessionManager.markStartup()

    application {
        val scope = rememberCoroutineScope()
        val cefState by dependencies.cefInitializer.initState.collectAsState()
        val webViewLifecycleManager = dependencies.webViewLifecycleManager
        var showCrashDialog by remember { mutableStateOf(needsCrashRecovery) }
        val sidebarViewModel =
            remember {
                SidebarViewModel(
                    accountManager = dependencies.accountManager,
                    servicePluginRegistry = dependencies.servicePluginRegistry,
                    webViewLifecycleCallback =
                        object : WebViewLifecycleCallback {
                            override fun onAccountDeleted(accountId: String) {
                                webViewLifecycleManager.destroyWebView(accountId)
                                sessionManager.saveImmediate()
                            }
                        },
                    scope = scope,
                )
            }

        LaunchedEffect(Unit) {
            dependencies.cefInitializer.initialize()
        }

        // Start periodic session save (30-second interval)
        LaunchedEffect(Unit) {
            sessionManager.startPeriodicSave(this)
        }

        // Track active account switches for background queue management
        val activeAccountId by dependencies.accountManager.activeAccountId.collectAsState()
        var previousAccountId by remember { mutableStateOf<String?>(null) }
        LaunchedEffect(activeAccountId) {
            activeAccountId?.let { newId ->
                webViewLifecycleManager.onAccountSwitched(previousAccountId, newId)
                sessionManager.saveImmediate()
                previousAccountId = newId
            }
        }

        // Start the suspend timer for background WebView cleanup
        LaunchedEffect(Unit) {
            webViewLifecycleManager.startSuspendTimer(this)
        }

        // Restore session if user chose to restore after crash
        LaunchedEffect(Unit) {
            if (!needsCrashRecovery && restoredSession == null) {
                // Normal startup: restore previous session silently
                val session = sessionManager.restoreSession()
                val activeId = session?.activeAccountId
                if (activeId != null) {
                    dependencies.accountManager.setActiveAccount(activeId)
                }
            }
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
                    // Crash recovery dialog (shown before main UI)
                    if (showCrashDialog) {
                        CrashRecoveryDialog(
                            onRestore = {
                                showCrashDialog = false
                                val activeId = restoredSession?.activeAccountId
                                if (activeId != null) {
                                    dependencies.accountManager.setActiveAccount(activeId)
                                }
                            },
                            onStartNew = {
                                showCrashDialog = false
                            },
                        )
                    } else {
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
                                        onUrlChanged = { accountId, url ->
                                            webViewLifecycleManager.updateAccountUrl(accountId, url)
                                        },
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
}
