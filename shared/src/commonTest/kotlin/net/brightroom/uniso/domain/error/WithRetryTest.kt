package net.brightroom.uniso.domain.error

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class WithRetryTest {
    @Test
    fun succeeds_on_first_attempt() =
        runTest {
            val result = withRetry { "ok" }
            assertEquals("ok", result)
        }

    @Test
    fun succeeds_after_transient_failure() =
        runTest {
            var attempts = 0
            val result =
                withRetry(initialDelayMs = 1) {
                    attempts++
                    if (attempts < 3) throw RuntimeException("transient")
                    "recovered"
                }
            assertEquals("recovered", result)
            assertEquals(3, attempts)
        }

    @Test
    fun throws_database_error_after_max_attempts() =
        runTest {
            var attempts = 0
            val error =
                assertFailsWith<AppError.DatabaseError> {
                    withRetry(maxAttempts = 3, initialDelayMs = 1) {
                        attempts++
                        throw RuntimeException("persistent failure")
                    }
                }
            assertEquals(3, attempts)
            assertEquals("persistent failure", error.cause.message)
        }

    @Test
    fun custom_max_attempts() =
        runTest {
            var attempts = 0
            assertFailsWith<AppError.DatabaseError> {
                withRetry(maxAttempts = 5, initialDelayMs = 1) {
                    attempts++
                    throw RuntimeException("fail")
                }
            }
            assertEquals(5, attempts)
        }

    @Test
    fun succeeds_on_last_attempt() =
        runTest {
            var attempts = 0
            val result =
                withRetry(maxAttempts = 3, initialDelayMs = 1) {
                    attempts++
                    if (attempts < 3) throw RuntimeException("not yet")
                    42
                }
            assertEquals(42, result)
            assertEquals(3, attempts)
        }
}
