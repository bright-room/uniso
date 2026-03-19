package net.brightroom.uniso.platform

class JvmPlatformPaths : PlatformPaths {
    override fun getAppDataDir(): String {
        val os = System.getProperty("os.name").lowercase()
        return when {
            os.contains("mac") -> {
                val home = System.getProperty("user.home")
                "$home/Library/Application Support/net.brightroom.uniso"
            }

            os.contains("win") -> {
                val appData =
                    System.getenv("APPDATA")
                        ?: (System.getProperty("user.home") + "/AppData/Roaming")
                "$appData/Uniso"
            }

            else -> {
                val home = System.getProperty("user.home")
                "$home/.local/share/uniso"
            }
        }
    }
}
