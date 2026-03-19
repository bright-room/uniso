package net.brightroom.uniso.di

import net.brightroom.uniso.data.DatabaseFactory
import net.brightroom.uniso.data.db.UnisoDatabase
import net.brightroom.uniso.data.repository.AccountRepository
import net.brightroom.uniso.data.repository.SessionRepository
import net.brightroom.uniso.domain.account.AccountManager
import net.brightroom.uniso.domain.plan.FreePlanProvider
import net.brightroom.uniso.domain.plan.PlanProvider
import net.brightroom.uniso.domain.plugin.ServicePluginRegistry
import net.brightroom.uniso.platform.KeychainAccessor
import net.brightroom.uniso.platform.PlatformPaths

class AppDependencies(
    platformPaths: PlatformPaths,
    keychainAccessor: KeychainAccessor,
) {
    private val databaseFactory = DatabaseFactory(keychainAccessor, platformPaths)
    val database: UnisoDatabase = databaseFactory.createDatabase()

    val accountRepository = AccountRepository(database)
    val sessionRepository = SessionRepository(database)

    val planProvider: PlanProvider = FreePlanProvider()
    val servicePluginRegistry = ServicePluginRegistry(database)

    val accountManager =
        AccountManager(
            accountRepository = accountRepository,
            sessionRepository = sessionRepository,
            planProvider = planProvider,
        )

    fun initialize() {
        accountManager.loadAccounts()
    }

    fun close() {
        databaseFactory.close()
    }
}
