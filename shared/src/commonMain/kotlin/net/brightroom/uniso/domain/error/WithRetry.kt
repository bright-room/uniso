package net.brightroom.uniso.domain.error

import kotlinx.coroutines.delay

/**
 * Executes [block] with retry logic using linear backoff.
 *
 * @param maxAttempts Maximum number of attempts (default: 3).
 * @param initialDelayMs Initial delay in milliseconds (default: 100). Each subsequent
 *   retry increases by [initialDelayMs] (i.e., 100ms, 200ms, 300ms).
 * @param block The suspending operation to execute.
 * @return The result of [block] if successful.
 * @throws AppError.DatabaseError if all attempts fail.
 */
suspend fun <T> withRetry(
    maxAttempts: Int = 3,
    initialDelayMs: Long = 100,
    block: suspend () -> T,
): T {
    var lastException: Throwable? = null

    repeat(maxAttempts) { attempt ->
        try {
            return block()
        } catch (e: Exception) {
            lastException = e
            if (attempt < maxAttempts - 1) {
                delay(initialDelayMs * (attempt + 1))
            }
        }
    }

    throw AppError.DatabaseError(lastException ?: IllegalStateException("All retry attempts failed"))
}
