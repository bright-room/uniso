package net.brightroom.uniso.domain.init

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import net.brightroom.uniso.data.repository.AccountRepository
import net.brightroom.uniso.data.repository.SessionRepository
import net.brightroom.uniso.data.repository.SqlSettingsRepository
import net.brightroom.uniso.data.repository.createTestDatabase
import net.brightroom.uniso.domain.account.AccountManager
import net.brightroom.uniso.domain.identity.IdentityManager
import net.brightroom.uniso.domain.plan.FreePlanProvider
import net.brightroom.uniso.domain.session.SessionManager
import net.brightroom.uniso.domain.settings.AppLocale
import net.brightroom.uniso.domain.settings.I18nManager
import net.brightroom.uniso.platform.PlatformLocale
import net.brightroom.uniso.platform.PlatformPaths
import net.brightroom.uniso.ui.webview.CefInitState
import net.brightroom.uniso.ui.webview.WebViewLifecycleManager
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AppInitializerTest {
    private lateinit var tempDir: File
    private lateinit var sessionRepository: SessionRepository
    private lateinit var accountRepository: AccountRepository
    private lateinit var accountManager: AccountManager
    private lateinit var sessionManager: SessionManager
    private lateinit var webViewLifecycleManager: WebViewLifecycleManager
    private lateinit var identityManager: IdentityManager
    private lateinit var settingsRepository: SqlSettingsRepository
    private lateinit var i18nManager: I18nManager

    @BeforeTest
    fun setup() {
        tempDir = File(System.getProperty("java.io.tmpdir"), "uniso-init-test-${System.nanoTime()}")
        tempDir.mkdirs()

        val database = createTestDatabase()
        sessionRepository = SessionRepository(database)
        accountRepository = AccountRepository(database)
        settingsRepository = SqlSettingsRepository(database)
        identityManager = IdentityManager(settingsRepository)
        accountManager =
            AccountManager(
                accountRepository = accountRepository,
                sessionRepository = sessionRepository,
                planProvider = FreePlanProvider(),
            )
        val platformPaths =
            object : PlatformPaths {
                override fun getAppDataDir(): String = tempDir.absolutePath
            }
        webViewLifecycleManager =
            WebViewLifecycleManager(
                platformPaths = platformPaths,
                accountStateSaver = { sessionRepository.saveAccountState(it) },
            )
        sessionManager =
            SessionManager(
                sessionRepository = sessionRepository,
                accountManager = accountManager,
                webViewLifecycleManager = webViewLifecycleManager,
            )
        val platformLocale =
            object : PlatformLocale {
                override fun getSystemLocale(): AppLocale = AppLocale.EN
            }
        i18nManager =
            I18nManager(
                platformLocale = platformLocale,
                settingsRepository = settingsRepository,
            )
    }

    @AfterTest
    fun teardown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun initialStateIsLoading() {
        val initializer = createInitializer()
        assertIs<InitState.Loading>(initializer.state.value)
    }

    @Test
    fun initializeCoreLoadsAccounts() {
        accountManager.loadAccounts()
        accountManager.addAccount("x")
        assertTrue(accountManager.accounts.value.isNotEmpty())

        val initializer = createInitializer()
        initializer.initializeCore()

        assertTrue(accountManager.accounts.value.isNotEmpty())
    }

    @Test
    fun initializeCoreCreatesLocalUserId() {
        val initializer = createInitializer()
        initializer.initializeCore()

        val userId = identityManager.getOrCreateLocalUserId()
        assertNotNull(userId)
        assertTrue(userId.isNotBlank())
    }

    @Test
    fun initializeCoreMarksStartup() {
        sessionManager.markCleanShutdown()
        assertTrue(sessionManager.isCleanShutdown())

        val initializer = createInitializer()
        initializer.initializeCore()

        val appState = sessionRepository.getAppState()
        assertNotNull(appState)
        assertEquals(false, appState.cleanShutdown)
    }

    @Test
    fun onCrashRestoreTransitionsToReadyAndSetsActiveAccount() {
        accountManager.loadAccounts()
        accountManager.addAccount("x")
        val accountId =
            accountManager.accounts.value
                .first()
                .accountId
        accountManager.setActiveAccount(accountId)
        webViewLifecycleManager.activateWebView(accountId)
        sessionManager.saveImmediate()

        val initializer = createInitializer()
        initializer.initializeCore()
        initializer.onCrashRestore()

        assertIs<InitState.Ready>(initializer.state.value)
        assertEquals(accountId, accountManager.activeAccountId.value)
    }

    @Test
    fun onCrashStartNewTransitionsToReady() {
        sessionManager.markStartup()

        val initializer = createInitializer()
        initializer.initializeCore()
        initializer.onCrashStartNew()

        assertIs<InitState.Ready>(initializer.state.value)
    }

    @Test
    fun initializeCefTransitionsToReadyOnCleanShutdown() =
        runTest {
            sessionManager.markCleanShutdown()

            val initializer = createInitializer()
            initializer.initializeCore()

            val fakeCefState = MutableStateFlow<CefInitState>(CefInitState.NotStarted)
            val job =
                launch {
                    initializer.initializeCefWithState(fakeCefState)
                }

            fakeCefState.value = CefInitState.Initializing
            advanceUntilIdle()
            assertIs<InitState.CefInitializing>(initializer.state.value)

            fakeCefState.value = CefInitState.Ready
            advanceUntilIdle()
            assertIs<InitState.Ready>(initializer.state.value)

            job.cancel()
        }

    @Test
    fun initializeCefShowsCrashPromptWhenCrashDetected() =
        runTest {
            sessionManager.markStartup()

            val initializer = createInitializer()
            initializer.initializeCore()

            val fakeCefState = MutableStateFlow<CefInitState>(CefInitState.NotStarted)
            val job =
                launch {
                    initializer.initializeCefWithState(fakeCefState)
                }

            fakeCefState.value = CefInitState.Ready
            advanceUntilIdle()
            assertIs<InitState.CrashRecoveryPrompt>(initializer.state.value)

            job.cancel()
        }

    @Test
    fun initializeCefHandlesError() =
        runTest {
            sessionManager.markCleanShutdown()

            val initializer = createInitializer()
            initializer.initializeCore()

            val fakeCefState = MutableStateFlow<CefInitState>(CefInitState.NotStarted)
            val job =
                launch {
                    initializer.initializeCefWithState(fakeCefState)
                }

            fakeCefState.value = CefInitState.Error("Test error")
            advanceUntilIdle()

            val state = initializer.state.value
            assertIs<InitState.Error>(state)
            assertTrue(state.error.message!!.contains("Test error"))

            job.cancel()
        }

    @Test
    fun initializeCefShowsDownloadProgress() =
        runTest {
            sessionManager.markCleanShutdown()

            val initializer = createInitializer()
            initializer.initializeCore()

            val fakeCefState = MutableStateFlow<CefInitState>(CefInitState.NotStarted)
            val job =
                launch {
                    initializer.initializeCefWithState(fakeCefState)
                }

            fakeCefState.value = CefInitState.Downloading(50f)
            advanceUntilIdle()

            val state = initializer.state.value
            assertIs<InitState.CefInitializing>(state)
            val cefState = state.cefState
            assertIs<CefInitState.Downloading>(cefState)
            assertEquals(50f, cefState.progress)

            fakeCefState.value = CefInitState.Ready
            advanceUntilIdle()

            job.cancel()
        }

    @Test
    fun startBackgroundTasksStartsPeriodicSave() =
        runTest {
            val initializer = createInitializer()
            initializer.initializeCore()

            val parentJob = Job()
            val childScope = kotlinx.coroutines.CoroutineScope(coroutineContext + parentJob)

            // Should not throw
            initializer.startBackgroundTasks(childScope)

            // Clean up background coroutines immediately
            parentJob.cancel()
        }

    private fun createInitializer(): AppInitializer =
        AppInitializer(
            accountManager = accountManager,
            sessionManager = sessionManager,
            webViewLifecycleManager = webViewLifecycleManager,
            identityManager = identityManager,
            i18nManager = i18nManager,
        )
}
