package net.brightroom.uniso.data

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import net.brightroom.uniso.data.db.UnisoDatabase
import net.brightroom.uniso.platform.KeychainAccessor
import net.brightroom.uniso.platform.PlatformPaths
import java.io.File
import java.security.SecureRandom

class DatabaseFactory(
    private val keychainAccessor: KeychainAccessor,
    private val platformPaths: PlatformPaths,
) {
    private var driver: JdbcSqliteDriver? = null

    fun createDatabase(): UnisoDatabase {
        val dbDir = File(platformPaths.getDatabaseDir())
        dbDir.mkdirs()

        val dbPath = File(dbDir, DB_NAME).absolutePath
        val jdbcUrl = "jdbc:sqlite:$dbPath"

        val newDriver =
            JdbcSqliteDriver(jdbcUrl).also {
                driver = it
            }

        newDriver.execute(null, "PRAGMA foreign_keys = ON;", 0)

        UnisoDatabase.Schema.create(newDriver)

        val database = UnisoDatabase(newDriver)

        insertInitialServicePlugins(database)

        return database
    }

    fun close() {
        driver?.close()
        driver = null
    }

    private fun insertInitialServicePlugins(database: UnisoDatabase) {
        database.servicePluginQueries.insertPlugin(
            service_id = "x",
            display_name = "X",
            domain_patterns = """["x.com","twitter.com"]""",
            brand_color = "#000000",
            icon_resource = "icons/x.svg",
            auth_type = "cookie",
            sort_order = 1,
        )
        database.servicePluginQueries.insertPlugin(
            service_id = "instagram",
            display_name = "Instagram",
            domain_patterns = """["instagram.com"]""",
            brand_color = "#E1306C",
            icon_resource = "icons/instagram.svg",
            auth_type = "cookie",
            sort_order = 2,
        )
        database.servicePluginQueries.insertPlugin(
            service_id = "facebook",
            display_name = "Facebook",
            domain_patterns = """["facebook.com"]""",
            brand_color = "#1877F2",
            icon_resource = "icons/facebook.svg",
            auth_type = "cookie",
            sort_order = 3,
        )
        database.servicePluginQueries.insertPlugin(
            service_id = "youtube",
            display_name = "YouTube",
            domain_patterns = """["youtube.com"]""",
            brand_color = "#FF0000",
            icon_resource = "icons/youtube.svg",
            auth_type = "cookie",
            sort_order = 4,
        )
        database.servicePluginQueries.insertPlugin(
            service_id = "bluesky",
            display_name = "Bluesky",
            domain_patterns = """["bsky.app"]""",
            brand_color = "#0085FF",
            icon_resource = "icons/bluesky.svg",
            auth_type = "cookie",
            sort_order = 5,
        )
        database.servicePluginQueries.insertPlugin(
            service_id = "twitch",
            display_name = "Twitch",
            domain_patterns = """["twitch.tv"]""",
            brand_color = "#9146FF",
            icon_resource = "icons/twitch.svg",
            auth_type = "cookie",
            sort_order = 6,
        )
    }

    companion object {
        private const val DB_KEY_ALIAS = "sns_dashboard_db_key"
        private const val DB_NAME = "uniso.db"

        fun createInMemory(): UnisoDatabase {
            val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
            driver.execute(null, "PRAGMA foreign_keys = ON;", 0)
            UnisoDatabase.Schema.create(driver)
            return UnisoDatabase(driver)
        }

        @Suppress("unused")
        fun generateEncryptionKey(): String {
            val bytes = ByteArray(32)
            SecureRandom().nextBytes(bytes)
            return bytes.joinToString("") { "%02x".format(it) }
        }
    }
}
