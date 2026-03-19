package net.brightroom.uniso.data.repository

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import net.brightroom.uniso.data.db.UnisoDatabase

fun createTestDatabase(): UnisoDatabase {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    driver.execute(null, "PRAGMA foreign_keys = ON;", 0)
    UnisoDatabase.Schema.create(driver)

    val database = UnisoDatabase(driver)

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

    return database
}
