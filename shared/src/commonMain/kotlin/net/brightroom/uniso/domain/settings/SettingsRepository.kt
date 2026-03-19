package net.brightroom.uniso.domain.settings

interface SettingsRepository {
    fun getString(key: String): String?

    fun setString(
        key: String,
        value: String,
    )

    fun getBoolean(key: String): Boolean?

    fun setBoolean(
        key: String,
        value: Boolean,
    )
}

class InMemorySettingsRepository : SettingsRepository {
    private val store = mutableMapOf<String, String>()

    override fun getString(key: String): String? = store[key]

    override fun setString(
        key: String,
        value: String,
    ) {
        store[key] = value
    }

    override fun getBoolean(key: String): Boolean? = store[key]?.toBooleanStrictOrNull()

    override fun setBoolean(
        key: String,
        value: Boolean,
    ) {
        store[key] = value.toString()
    }
}
