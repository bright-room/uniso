package net.brightroom.uniso.ui

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type

/**
 * Actions that can be triggered by keyboard shortcuts.
 */
enum class ShortcutAction {
    NEXT_ACCOUNT,
    PREVIOUS_ACCOUNT,
    ADD_ACCOUNT,
    OPEN_SETTINGS,
    RELOAD,
    FORCE_RELOAD,
    CLOSE_WINDOW,
}

/**
 * Handles global keyboard shortcuts for the application.
 *
 * Shortcut mappings:
 * - Ctrl+Tab: Next account
 * - Ctrl+Shift+Tab: Previous account
 * - Cmd+N (macOS) / Ctrl+N (Windows): Add account
 * - Cmd+, (macOS) / Ctrl+, (Windows): Open settings
 * - Cmd+R (macOS) / Ctrl+R (Windows): Reload
 * - Cmd+Shift+R (macOS) / Ctrl+Shift+R (Windows): Force reload
 * - Cmd+W (macOS) / Ctrl+W (Windows): Close window
 */
object KeyboardShortcutHandler {
    private val isMac = System.getProperty("os.name").orEmpty().contains("Mac", ignoreCase = true)

    /**
     * Resolves a Compose KeyEvent to a ShortcutAction, or null if no shortcut matches.
     * Only processes KeyDown events to avoid double-firing.
     */
    fun resolve(event: KeyEvent): ShortcutAction? {
        if (event.type != KeyEventType.KeyDown) return null

        val ctrl = event.isCtrlPressed
        val meta = event.isMetaPressed
        val shift = event.isShiftPressed
        val platformMod = if (isMac) meta else ctrl

        return when {
            // Ctrl+Shift+Tab → Previous account (check before Ctrl+Tab)
            ctrl && shift && event.key == Key.Tab -> ShortcutAction.PREVIOUS_ACCOUNT

            // Ctrl+Tab → Next account
            ctrl && !shift && event.key == Key.Tab -> ShortcutAction.NEXT_ACCOUNT

            // Cmd/Ctrl+N → Add account
            platformMod && !shift && event.key == Key.N -> ShortcutAction.ADD_ACCOUNT

            // Cmd/Ctrl+, → Open settings
            platformMod && !shift && event.key == Key.Comma -> ShortcutAction.OPEN_SETTINGS

            // Cmd/Ctrl+Shift+R → Force reload (check before Cmd/Ctrl+R)
            platformMod && shift && event.key == Key.R -> ShortcutAction.FORCE_RELOAD

            // Cmd/Ctrl+R → Reload
            platformMod && !shift && event.key == Key.R -> ShortcutAction.RELOAD

            // Cmd/Ctrl+W → Close window
            platformMod && !shift && event.key == Key.W -> ShortcutAction.CLOSE_WINDOW

            else -> null
        }
    }

    /**
     * Resolves a shortcut from raw key information. Used for testing without Compose KeyEvent.
     */
    internal fun resolveFromRaw(
        keyCode: Long,
        isCtrl: Boolean,
        isShift: Boolean,
        isMeta: Boolean,
        isKeyDown: Boolean,
    ): ShortcutAction? {
        if (!isKeyDown) return null

        val platformMod = if (isMac) isMeta else isCtrl
        val key = Key(keyCode)

        return when {
            isCtrl && isShift && key == Key.Tab -> ShortcutAction.PREVIOUS_ACCOUNT
            isCtrl && !isShift && key == Key.Tab -> ShortcutAction.NEXT_ACCOUNT
            platformMod && !isShift && key == Key.N -> ShortcutAction.ADD_ACCOUNT
            platformMod && !isShift && key == Key.Comma -> ShortcutAction.OPEN_SETTINGS
            platformMod && isShift && key == Key.R -> ShortcutAction.FORCE_RELOAD
            platformMod && !isShift && key == Key.R -> ShortcutAction.RELOAD
            platformMod && !isShift && key == Key.W -> ShortcutAction.CLOSE_WINDOW
            else -> null
        }
    }
}
