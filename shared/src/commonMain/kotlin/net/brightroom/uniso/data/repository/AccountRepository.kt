package net.brightroom.uniso.data.repository

import net.brightroom.uniso.data.db.UnisoDatabase
import net.brightroom.uniso.data.model.Account

class AccountRepository(
    private val database: UnisoDatabase,
) {
    fun insert(account: Account) {
        database.accountQueries.insertAccount(
            account_id = account.accountId,
            service_id = account.serviceId,
            display_name = account.displayName,
            avatar_url = account.avatarUrl,
            sort_order = account.sortOrder.toLong(),
            created_at = account.createdAt,
        )
    }

    fun delete(accountId: String) {
        database.accountQueries.deleteAccount(accountId)
    }

    fun getAll(): List<Account> =
        database.accountQueries.selectAll().executeAsList().map { row ->
            Account(
                accountId = row.account_id,
                serviceId = row.service_id,
                displayName = row.display_name,
                avatarUrl = row.avatar_url,
                sortOrder = row.sort_order.toInt(),
                createdAt = row.created_at,
            )
        }

    fun getByServiceId(serviceId: String): List<Account> =
        database.accountQueries.selectByServiceId(serviceId).executeAsList().map { row ->
            Account(
                accountId = row.account_id,
                serviceId = row.service_id,
                displayName = row.display_name,
                avatarUrl = row.avatar_url,
                sortOrder = row.sort_order.toInt(),
                createdAt = row.created_at,
            )
        }

    fun getCount(): Int =
        database.accountQueries
            .selectCount()
            .executeAsOne()
            .toInt()

    fun updateDisplayName(
        accountId: String,
        name: String,
    ) {
        database.accountQueries.updateDisplayName(name, accountId)
    }

    fun updateSortOrder(
        accountId: String,
        sortOrder: Int,
    ) {
        database.accountQueries.updateSortOrder(sortOrder.toLong(), accountId)
    }
}
