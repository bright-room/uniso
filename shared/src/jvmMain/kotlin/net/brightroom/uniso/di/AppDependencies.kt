package net.brightroom.uniso.di

import net.brightroom.uniso.data.DatabaseFactory
import net.brightroom.uniso.data.db.UnisoDatabase
import net.brightroom.uniso.data.repository.AccountRepository
import net.brightroom.uniso.data.repository.SessionRepository
import net.brightroom.uniso.data.repository.SqlSettingsRepository
import net.brightroom.uniso.domain.account.AccountManager
import net.brightroom.uniso.domain.plan.FreePlanProvider
import net.brightroom.uniso.domain.plan.PlanProvider
import net.brightroom.uniso.domain.plugin.ServicePluginRegistry
import net.brightroom.uniso.domain.settings.I18nManager
import net.brightroom.uniso.domain.settings.SettingsRepository
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

    val planProvider: PlanProvider = FreePlanProvider()
    val servicePluginRegistry = ServicePluginRegistry(database)

    val i18nManager =
        I18nManager(
            platformLocale = platformLocale,
            settingsRepository = settingsRepository,
        )

    val accountManager =
        AccountManager(
            accountRepository = accountRepository,
            sessionRepository = sessionRepository,
            planProvider = planProvider,
        )

    val cefInitializer = CefInitializer(platformPaths)

    val webViewLifecycleManager = WebViewLifecycleManager(platformPaths)

    fun initialize() {
        i18nManager.initialize()
        accountManager.loadAccounts()
    }

    fun close() {
        webViewLifecycleManager.destroyAll()
        cefInitializer.dispose()
        databaseFactory.close()
    }
}
