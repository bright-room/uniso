package net.brightroom.uniso.di

import net.brightroom.uniso.data.DatabaseFactory
import net.brightroom.uniso.data.db.UnisoDatabase
import net.brightroom.uniso.data.model.AccountState
import net.brightroom.uniso.data.repository.AccountRepository
import net.brightroom.uniso.data.repository.SessionRepository
import net.brightroom.uniso.data.repository.SqlSettingsRepository
import net.brightroom.uniso.domain.account.AccountManager
import net.brightroom.uniso.domain.identity.IdentityManager
import net.brightroom.uniso.domain.link.LinkRouter
import net.brightroom.uniso.domain.plugin.ServicePluginRegistry
import net.brightroom.uniso.domain.session.SessionManager
import net.brightroom.uniso.domain.settings.I18nManager
import net.brightroom.uniso.domain.settings.SettingsRepository
import net.brightroom.uniso.domain.updater.AutoUpdater
import net.brightroom.uniso.domain.updater.JvmAutoUpdater
import net.brightroom.uniso.platform.KeychainAccessor
import net.brightroom.uniso.platform.PlatformLocale
import net.brightroom.uniso.platform.PlatformPaths
import net.brightroom.uniso.ui.webview.CefInitializer
import net.brightroom.uniso.ui.webview.WebViewLifecycleManager

class AppDependencies(
    private val platformPaths: PlatformPaths,
    keychainAccessor: KeychainAccessor,
    platformLocale: PlatformLocale,
) {
    private val databaseFactory = DatabaseFactory(keychainAccessor, platformPaths)
    val database: UnisoDatabase = databaseFactory.createDatabase()

    val accountRepository = AccountRepository(database)
    val sessionRepository = SessionRepository(database)
    val settingsRepository: SettingsRepository = SqlSettingsRepository(database)

    val servicePluginRegistry = ServicePluginRegistry(database)

    val i18nManager =
        I18nManager(
            platformLocale = platformLocale,
            settingsRepository = settingsRepository,
        )

    val identityManager =
        IdentityManager(
            settingsRepository = settingsRepository as SqlSettingsRepository,
        )

    val accountManager =
        AccountManager(
            accountRepository = accountRepository,
            sessionRepository = sessionRepository,
        )

    val linkRouter =
        LinkRouter(
            servicePluginRegistry = servicePluginRegistry,
            accountManager = accountManager,
        )

    val cefInitializer = CefInitializer(platformPaths)

    val webViewLifecycleManager =
        WebViewLifecycleManager(
            platformPaths = platformPaths,
            accountStateSaver = { state: AccountState ->
                sessionRepository.saveAccountState(state)
            },
        )

    val sessionManager =
        SessionManager(
            sessionRepository = sessionRepository,
            accountManager = accountManager,
            webViewLifecycleManager = webViewLifecycleManager,
        )

    val autoUpdater: AutoUpdater =
        JvmAutoUpdater(
            appcastUrl = APPCAST_URL,
        )

    fun initialize() {
        i18nManager.initialize()
        accountManager.loadAccounts()
    }

    fun close() {
        sessionManager.stopPeriodicSave()
        sessionManager.saveImmediate()
        sessionManager.markCleanShutdown()
        webViewLifecycleManager.destroyAll()
        cefInitializer.dispose()
        databaseFactory.close()
    }

    companion object {
        const val APPCAST_URL = "https://updates.brightroom.net/uniso/appcast.xml"
    }
}
