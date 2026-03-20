package net.brightroom.uniso.domain.error

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AppErrorTest {
    @Test
    fun webViewLoadFailed_containsUrlAndErrorInfo() {
        val error = AppError.WebViewLoadFailed(url = "https://x.com", errorCode = -105, errorText = "ERR_NAME_NOT_RESOLVED")
        assertIs<AppError>(error)
        assertEquals("https://x.com", error.url)
        assertEquals(-105, error.errorCode)
        assertEquals("ERR_NAME_NOT_RESOLVED", error.errorText)
        assertTrue(error.message.contains("x.com"))
        assertTrue(error.message.contains("-105"))
    }

    @Test
    fun cefInitFailed_wrapsCause() {
        val cause = RuntimeException("binary missing")
        val error = AppError.CefInitFailed(cause)
        assertIs<AppError>(error)
        assertEquals(cause, error.cause)
        assertTrue(error.message.contains("binary missing"))
    }

    @Test
    fun databaseError_wrapsCause() {
        val cause = IllegalStateException("lock timeout")
        val error = AppError.DatabaseError(cause)
        assertIs<AppError>(error)
        assertEquals(cause, error.cause)
        assertTrue(error.message.contains("lock timeout"))
    }

    @Test
    fun sessionRestoreFailed_containsAccountId() {
        val cause = RuntimeException("profile corrupted")
        val error = AppError.SessionRestoreFailed("acc-123", cause)
        assertIs<AppError>(error)
        assertEquals("acc-123", error.accountId)
        assertEquals(cause, error.cause)
        assertTrue(error.message.contains("acc-123"))
    }

    @Test
    fun updateFailed_containsVersion() {
        val cause = RuntimeException("download failed")
        val error = AppError.UpdateFailed("2.0.0", cause)
        assertIs<AppError>(error)
        assertEquals("2.0.0", error.version)
        assertTrue(error.message.contains("2.0.0"))
    }
}
