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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import net.brightroom.uniso.di.AppDependencies
import net.brightroom.uniso.domain.init.AppInitializer
import net.brightroom.uniso.domain.init.InitState
import net.brightroom.uniso.platform.ExternalBrowserLauncher
import net.brightroom.uniso.platform.JvmKeychainAccessor
import net.brightroom.uniso.platform.JvmPlatformLocale
import net.brightroom.uniso.platform.JvmPlatformPaths
import net.brightroom.uniso.ui.KeyboardShortcutHandler
import net.brightroom.uniso.ui.LocalI18n
import net.brightroom.uniso.ui.MainLayout
import net.brightroom.uniso.ui.MainScreen
import net.brightroom.uniso.ui.ShortcutAction
import net.brightroom.uniso.ui.dialogs.CrashRecoveryDialog
import net.brightroom.uniso.ui.dialogs.UpdateDialog
import net.brightroom.uniso.ui.onboarding.TutorialScreen
import net.brightroom.uniso.ui.settings.SettingsViewModel
import net.brightroom.uniso.ui.sidebar.ExternalBrowserCallback
import net.brightroom.uniso.ui.sidebar.SidebarViewModel
import net.brightroom.uniso.ui.sidebar.WebViewLifecycleCallback
import net.brightroom.uniso.ui.theme.AppTheme
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
    val initializer =
        AppInitializer(
            accountManager = dependencies.accountManager,
            sessionManager = dependencies.sessionManager,
            webViewLifecycleManager = dependencies.webViewLifecycleManager,
            identityManager = dependencies.identityManager,
            i18nManager = dependencies.i18nManager,
            settingsRepository = dependencies.settingsRepository,
            cefInitializer = dependencies.cefInitializer,
            autoUpdater = dependencies.autoUpdater,
        )

    // Phase 1: Core initialization (DB, i18n, accounts, crash check)
    initializer.initializeCore()

    application {
        val scope = rememberCoroutineScope()
        val initState by initializer.state.collectAsState()
        val webViewLifecycleManager = dependencies.webViewLifecycleManager
        val sessionManager = dependencies.sessionManager

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
                    autoUpdater = dependencies.autoUpdater,
                    scope = scope,
                )
            }

        // Phase 2: CEF initialization (background)
        LaunchedEffect(Unit) {
            initializer.initializeCef()
        }

        // Start background tasks once ready
        var backgroundTasksStarted by remember { mutableStateOf(false) }
        LaunchedEffect(initState) {
            if (initState is InitState.Ready && !backgroundTasksStarted) {
                backgroundTasksStarted = true
                initializer.startBackgroundTasks(scope)
            }
        }

        // Track active account switches for background queue management
        val activeAccountId by dependencies.accountManager.activeAccountId.collectAsState()
        var previousAccountId by remember { mutableStateOf<String?>(null) }
        LaunchedEffect(activeAccountId) {
            if (initState is InitState.Ready) {
                activeAccountId?.let { newId ->
                    webViewLifecycleManager.onAccountSwitched(previousAccountId, newId)
                    sessionManager.saveImmediate()
                    previousAccountId = newId
                }
            }
        }

        val navigatorRegistry = remember { WebViewNavigatorRegistry() }
        var currentScreen by remember { mutableStateOf<MainScreen>(MainScreen.WebView) }

        val handleKeyEvent: (KeyEvent) -> Boolean = handler@{ event ->
            if (initState !is InitState.Ready) return@handler false
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
            icon = painterResource("icons/icon.png"),
            state = rememberWindowState(width = 1280.dp, height = 800.dp),
            onKeyEvent = handleKeyEvent,
        ) {
            AppTheme {
                CompositionLocalProvider(LocalI18n provides dependencies.i18nManager) {
                    val appIcon = painterResource("icons/icon.png")

                    when (val currentState = initState) {
                        is InitState.Loading,
                        is InitState.CefInitializing,
                        is InitState.Error,
                        -> {
                            SplashScreen(initState = currentState, appIcon = appIcon)
                        }

                        is InitState.CrashRecoveryPrompt -> {
                            CrashRecoveryDialog(
                                onRestore = { initializer.onCrashRestore() },
                                onStartNew = { initializer.onCrashStartNew() },
                            )
                        }

                        is InitState.TutorialRequired -> {
                            TutorialScreen(
                                services = dependencies.servicePluginRegistry.getAll(),
                                onComplete = { initializer.onTutorialComplete() },
                                appIcon = painterResource("icons/icon.png"),
                            )
                        }

                        is InitState.Ready -> {
                            val sidebarAccounts by sidebarViewModel.sidebarAccounts.collectAsState()
                            val activatedIds by webViewLifecycleManager.activatedAccountIds.collectAsState()
                            val activatedAccounts = sidebarAccounts.filter { it.accountId in activatedIds }
                            val pendingUpdate by dependencies.autoUpdater.updateInfo.collectAsState()

                            pendingUpdate?.let { info ->
                                UpdateDialog(
                                    version = info.version,
                                    onUpdate = {
                                        dependencies.autoUpdater.dismissUpdate()
                                        ExternalBrowserLauncher.open(info.downloadUrl)
                                    },
                                    onDismiss = {
                                        dependencies.autoUpdater.dismissUpdate()
                                    },
                                )
                            }

                            MainLayout(
                                viewModel = sidebarViewModel,
                                settingsViewModel = settingsViewModel,
                                activatedAccounts = activatedAccounts,
                                currentScreen = currentScreen,
                                onScreenChange = { currentScreen = it },
                                webViewReady = true,
                                onShowTutorial = {
                                    settingsViewModel.resetTutorial()
                                    initializer.showTutorial()
                                },
                                onCheckForUpdates = {
                                    settingsViewModel.checkForUpdates()
                                },
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
                        }
                    }
                }
            }
        }
    }
}
