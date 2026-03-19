package net.brightroom.uniso.data.repository

import net.brightroom.uniso.data.db.UnisoDatabase
import net.brightroom.uniso.data.model.AccountState
import net.brightroom.uniso.data.model.AppState

class SessionRepository(
    private val database: UnisoDatabase,
) {
    fun saveAppState(state: AppState) {
        database.appStateQueries.upsertAppState(
            active_account_id = state.activeAccountId,
            clean_shutdown = if (state.cleanShutdown) 1L else 0L,
            last_saved_at = state.lastSavedAt,
        )
    }

    fun getAppState(): AppState? =
        database.appStateQueries
            .selectAppState()
            .executeAsOneOrNull()
            ?.let { row ->
                AppState(
                    activeAccountId = row.active_account_id,
                    cleanShutdown = row.clean_shutdown != 0L,
                    lastSavedAt = row.last_saved_at,
                )
            }

    fun saveAccountState(state: AccountState) {
        database.accountStateQueries.upsertAccountState(
            account_id = state.accountId,
            last_url = state.lastUrl,
            scroll_position_y = state.scrollPositionY.toLong(),
            webview_status = state.webviewStatus,
            last_accessed_at = state.lastAccessedAt,
        )
    }

    fun getAccountState(accountId: String): AccountState? =
        database.accountStateQueries
            .selectByAccountId(accountId)
            .executeAsOneOrNull()
            ?.let { row ->
                AccountState(
                    accountId = row.account_id,
                    lastUrl = row.last_url,
                    scrollPositionY = row.scroll_position_y.toInt(),
                    webviewStatus = row.webview_status,
                    lastAccessedAt = row.last_accessed_at,
                )
            }

    fun getAllAccountStates(): List<AccountState> =
        database.accountStateQueries.selectAll().executeAsList().map { row ->
            AccountState(
                accountId = row.account_id,
                lastUrl = row.last_url,
                scrollPositionY = row.scroll_position_y.toInt(),
                webviewStatus = row.webview_status,
                lastAccessedAt = row.last_accessed_at,
            )
        }

    fun deleteAccountState(accountId: String) {
        database.accountStateQueries.deleteByAccountId(accountId)
    }
}
