package net.brightroom.uniso.platform

interface PlatformPaths {
    fun getAppDataDir(): String

    fun getDatabaseDir(): String = getAppDataDir() + "/db"

    fun getCefProfileDir(accountId: String): String = getAppDataDir() + "/cef_profiles/" + accountId
}
