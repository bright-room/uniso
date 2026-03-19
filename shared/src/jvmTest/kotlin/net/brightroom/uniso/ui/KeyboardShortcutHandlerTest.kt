package net.brightroom.uniso.ui

import androidx.compose.ui.input.key.Key
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class KeyboardShortcutHandlerTest {
    private val isMac = System.getProperty("os.name").orEmpty().contains("Mac", ignoreCase = true)

    private fun resolve(
        key: Key,
        ctrl: Boolean = false,
        shift: Boolean = false,
        meta: Boolean = false,
        isKeyDown: Boolean = true,
    ): ShortcutAction? =
        KeyboardShortcutHandler.resolveFromRaw(
            keyCode = key.keyCode,
            isCtrl = ctrl,
            isShift = shift,
            isMeta = meta,
            isKeyDown = isKeyDown,
        )

    @Test
    fun ctrlTabResolvesToNextAccount() {
        assertEquals(ShortcutAction.NEXT_ACCOUNT, resolve(Key.Tab, ctrl = true))
    }

    @Test
    fun ctrlShiftTabResolvesToPreviousAccount() {
        assertEquals(ShortcutAction.PREVIOUS_ACCOUNT, resolve(Key.Tab, ctrl = true, shift = true))
    }

    @Test
    fun keyUpReturnsNull() {
        assertNull(resolve(Key.Tab, ctrl = true, isKeyDown = false))
    }

    @Test
    fun unmappedKeyReturnsNull() {
        assertNull(resolve(Key.A))
    }

    @Test
    fun platformModNResolvesToAddAccount() {
        val action =
            if (isMac) {
                resolve(Key.N, meta = true)
            } else {
                resolve(Key.N, ctrl = true)
            }
        assertEquals(ShortcutAction.ADD_ACCOUNT, action)
    }

    @Test
    fun platformModCommaResolvesToOpenSettings() {
        val action =
            if (isMac) {
                resolve(Key.Comma, meta = true)
            } else {
                resolve(Key.Comma, ctrl = true)
            }
        assertEquals(ShortcutAction.OPEN_SETTINGS, action)
    }

    @Test
    fun platformModRResolvesToReload() {
        val action =
            if (isMac) {
                resolve(Key.R, meta = true)
            } else {
                resolve(Key.R, ctrl = true)
            }
        assertEquals(ShortcutAction.RELOAD, action)
    }

    @Test
    fun platformModShiftRResolvesToForceReload() {
        val action =
            if (isMac) {
                resolve(Key.R, meta = true, shift = true)
            } else {
                resolve(Key.R, ctrl = true, shift = true)
            }
        assertEquals(ShortcutAction.FORCE_RELOAD, action)
    }

    @Test
    fun platformModWResolvesToCloseWindow() {
        val action =
            if (isMac) {
                resolve(Key.W, meta = true)
            } else {
                resolve(Key.W, ctrl = true)
            }
        assertEquals(ShortcutAction.CLOSE_WINDOW, action)
    }

    @Test
    fun wrongModifierDoesNotMatch() {
        // On Mac, Ctrl+N should NOT resolve to ADD_ACCOUNT (needs Meta)
        // On Windows, Meta+N should NOT resolve to ADD_ACCOUNT (needs Ctrl)
        if (isMac) {
            assertNull(resolve(Key.N, ctrl = true))
        } else {
            assertNull(resolve(Key.N, meta = true))
        }
    }
}
