package net.brightroom.uniso.data.repository

import net.brightroom.uniso.data.db.UnisoDatabase
import net.brightroom.uniso.data.model.LocalUser
import net.brightroom.uniso.domain.settings.SettingsRepository

class SqlSettingsRepository(
    private val database: UnisoDatabase,
) : SettingsRepository {
    override fun getString(key: String): String? = database.settingsQueries.selectByKey(key).executeAsOneOrNull()

    override fun setString(
        key: String,
        value: String,
    ) {
        database.settingsQueries.upsertSetting(key, value)
    }

    override fun getBoolean(key: String): Boolean? = getString(key)?.toBooleanStrictOrNull()

    override fun setBoolean(
        key: String,
        value: Boolean,
    ) {
        setString(key, value.toString())
    }

    fun getLocalUser(): LocalUser? =
        database.localUserQueries
            .selectLocalUser()
            .executeAsOneOrNull()
            ?.let { row ->
                LocalUser(id = row.id, createdAt = row.created_at)
            }

    fun insertLocalUser(user: LocalUser) {
        database.localUserQueries.insertLocalUser(user.id, user.createdAt)
    }
}
