package net.brightroom.uniso.domain.updater

/**
 * Compares semantic version strings (e.g. "1.2.3").
 * Returns true if [remoteVersion] is newer than [currentVersion].
 */
object VersionComparator {
    fun isNewer(
        currentVersion: String,
        remoteVersion: String,
    ): Boolean {
        val current = parseVersion(currentVersion)
        val remote = parseVersion(remoteVersion)

        for (i in 0 until maxOf(current.size, remote.size)) {
            val c = current.getOrElse(i) { 0 }
            val r = remote.getOrElse(i) { 0 }
            if (r > c) return true
            if (r < c) return false
        }
        return false
    }

    private fun parseVersion(version: String): List<Int> = version.split(".").mapNotNull { it.toIntOrNull() }
}
