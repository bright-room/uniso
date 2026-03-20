package net.brightroom.uniso.domain.error

/**
 * Application error type hierarchy.
 * Each subtype represents a specific category of error with relevant contextual data.
 */
sealed class AppError : Exception() {
    data class WebViewLoadFailed(
        val url: String,
        val errorCode: Int,
        val errorText: String,
    ) : AppError() {
        override val message: String
            get() = "WebView load failed: $errorText (code=$errorCode, url=$url)"
    }

    data class CefInitFailed(
        override val cause: Throwable,
    ) : AppError() {
        override val message: String
            get() = "CEF initialization failed: ${cause.message}"
    }

    data class DatabaseError(
        override val cause: Throwable,
    ) : AppError() {
        override val message: String
            get() = "Database error: ${cause.message}"
    }

    data class SessionRestoreFailed(
        val accountId: String,
        override val cause: Throwable,
    ) : AppError() {
        override val message: String
            get() = "Session restore failed for account $accountId: ${cause.message}"
    }

    data class UpdateFailed(
        val version: String,
        override val cause: Throwable,
    ) : AppError() {
        override val message: String
            get() = "Update to version $version failed: ${cause.message}"
    }
}
