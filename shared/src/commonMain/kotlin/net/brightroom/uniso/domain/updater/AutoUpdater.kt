package net.brightroom.uniso.domain.updater

import kotlinx.coroutines.flow.StateFlow

/**
 * Abstraction for checking application updates.
 * Platform implementations use Sparkle (macOS) or WinSparkle (Windows).
 */
interface AutoUpdater {
    /** Latest available update info, or null if up-to-date / not yet checked. */
    val updateInfo: StateFlow<UpdateInfo?>

    /** Whether an update check is currently in progress. */
    val isChecking: StateFlow<Boolean>

    /** Trigger an interactive update check (shows UI feedback). */
    suspend fun checkForUpdates()

    /** Trigger a silent background update check (no UI on "up-to-date"). */
    suspend fun checkForUpdatesInBackground()

    /** Dismiss the current update notification. */
    fun dismissUpdate()
}
