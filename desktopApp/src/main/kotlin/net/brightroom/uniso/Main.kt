package net.brightroom.uniso

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import net.brightroom.uniso.di.AppDependencies
import net.brightroom.uniso.platform.ExternalBrowserLauncher
import net.brightroom.uniso.platform.JvmKeychainAccessor
import net.brightroom.uniso.platform.JvmPlatformLocale
import net.brightroom.uniso.platform.JvmPlatformPaths
import net.brightroom.uniso.ui.KeyboardShortcutHandler
import net.brightroom.uniso.ui.LocalI18n
import net.brightroom.uniso.ui.MainLayout
import net.brightroom.uniso.ui.MainScreen
import net.brightroom.uniso.ui.ShortcutAction
import net.brightroom.uniso.ui.dialogs.CefInitErrorDialog
import net.brightroom.uniso.ui.dialogs.CrashRecoveryDialog
import net.brightroom.uniso.ui.settings.SettingsViewModel
import net.brightroom.uniso.ui.sidebar.ExternalBrowserCallback
import net.brightroom.uniso.ui.sidebar.SidebarViewModel
import net.brightroom.uniso.ui.sidebar.WebViewLifecycleCallback
import net.brightroom.uniso.ui.theme.AppTheme
import net.brightroom.uniso.ui.webview.CefInitState
import net.brightroom.uniso.ui.webview.SplashScreen
import net.brightroom.uniso.ui.webview.WebViewNavigatorRegistry
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
        val webViewCleanup: (String) -> Unit = { accountId ->
            webViewLifecycleManager.destroyWebView(accountId)
            sessionManager.saveImmediate()
        }
        val sidebarViewModel =
            remember {
                SidebarViewModel(
                    accountManager = dependencies.accountManager,
                    servicePluginRegistry = dependencies.servicePluginRegistry,
                    webViewLifecycleCallback =
                        object : WebViewLifecycleCallback {
                            override fun onAccountDeleted(accountId: String) {
                                webViewCleanup(accountId)
                            }
                        },
                    externalBrowserCallback =
                        ExternalBrowserCallback { url ->
                            ExternalBrowserLauncher.open(url)
                        },
                    scope = scope,
                )
            }
        val settingsViewModel =
            remember {
                SettingsViewModel(
                    accountManager = dependencies.accountManager,
                    servicePluginRegistry = dependencies.servicePluginRegistry,
                    i18nManager = dependencies.i18nManager,
                    settingsRepository = dependencies.settingsRepository,
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

        val navigatorRegistry = remember { WebViewNavigatorRegistry() }
        var currentScreen by remember { mutableStateOf<MainScreen>(MainScreen.WebView) }

        val handleKeyEvent: (KeyEvent) -> Boolean = handler@{ event ->
            val action = KeyboardShortcutHandler.resolve(event) ?: return@handler false
            when (action) {
                ShortcutAction.NEXT_ACCOUNT -> {
                    currentScreen = MainScreen.WebView
                    dependencies.accountManager.switchToNextAccount()
                }

                ShortcutAction.PREVIOUS_ACCOUNT -> {
                    currentScreen = MainScreen.WebView
                    dependencies.accountManager.switchToPreviousAccount()
                }

                ShortcutAction.ADD_ACCOUNT -> {
                    sidebarViewModel.onAddAccountClick()
                }

                ShortcutAction.OPEN_SETTINGS -> {
                    currentScreen =
                        if (currentScreen is MainScreen.Settings) MainScreen.WebView else MainScreen.Settings
                }

                ShortcutAction.RELOAD -> {
                    val activeId = dependencies.accountManager.activeAccountId.value
                    if (activeId != null) navigatorRegistry.reload(activeId)
                }

                ShortcutAction.FORCE_RELOAD -> {
                    val activeId = dependencies.accountManager.activeAccountId.value
                    if (activeId != null) navigatorRegistry.forceReload(activeId)
                }

                ShortcutAction.CLOSE_WINDOW -> {
                    dependencies.close()
                    exitApplication()
                }
            }
            true
        }

        Window(
            onCloseRequest = {
                dependencies.close()
                exitApplication()
            },
            title = "${Constants.APP_NAME} — ${getPlatformName()}",
            state = rememberWindowState(width = 1280.dp, height = 800.dp),
            onKeyEvent = handleKeyEvent,
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
                        val cefError = cefState as? CefInitState.Error
                        val sidebarAccounts by sidebarViewModel.sidebarAccounts.collectAsState()
                        val activatedIds by webViewLifecycleManager.activatedAccountIds.collectAsState()
                        val activatedAccounts = sidebarAccounts.filter { it.accountId in activatedIds }

                        if (webViewReady) {
                            MainLayout(
                                viewModel = sidebarViewModel,
                                settingsViewModel = settingsViewModel,
                                activatedAccounts = activatedAccounts,
                                currentScreen = currentScreen,
                                onScreenChange = { currentScreen = it },
                                webViewReady = true,
                                webViewContent = { accounts, activeId, visible ->
                                    WebViewPanel(
                                        accounts = accounts,
                                        activeAccountId = activeId,
                                        visible = visible,
                                        linkRouter = dependencies.linkRouter,
                                        navigatorRegistry = navigatorRegistry,
                                        onUrlChanged = { accountId, url ->
                                            webViewLifecycleManager.updateAccountUrl(accountId, url)
                                        },
                                        onAccountSwitch = { accountId, _ ->
                                            dependencies.accountManager.setActiveAccount(accountId)
                                        },
                                    )
                                },
                                onWebViewCleanup = webViewCleanup,
                            )
                        } else if (cefError != null) {
                            CefInitErrorDialog(
                                errorMessage = cefError.message,
                                onDismiss = {
                                    dependencies.close()
                                    exitApplication()
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
