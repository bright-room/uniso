package net.brightroom.uniso.ui.webview

import net.brightroom.uniso.domain.settings.I18nManager
import net.brightroom.uniso.domain.settings.StringKey

/**
 * Generates a custom HTML error page for WebView load failures.
 *
 * The error page displays a service-themed icon, localized error message,
 * a retry button, and an "open in browser" link.
 */
object WebViewErrorPage {
    fun generate(
        failedUrl: String,
        errorCode: Int,
        errorText: String,
        i18nManager: I18nManager,
    ): String {
        val connectionFailed = escapeHtml(i18nManager.getString(StringKey.ERROR_CONNECTION_FAILED))
        val retry = escapeHtml(i18nManager.getString(StringKey.ERROR_RETRY))
        val openInBrowser = escapeHtml(i18nManager.getString(StringKey.ERROR_OPEN_IN_BROWSER))
        val escapedUrl = escapeHtml(failedUrl)
        val escapedError = escapeHtml(errorText)

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                        display: flex;
                        justify-content: center;
                        align-items: center;
                        min-height: 100vh;
                        background: #f5f5f5;
                        color: #333;
                    }
                    @media (prefers-color-scheme: dark) {
                        body { background: #1a1a1a; color: #e0e0e0; }
                        .card { background: #2a2a2a; }
                        .error-detail { color: #999; }
                        .retry-btn { background: #e0e0e0; color: #1a1a1a; }
                        .retry-btn:hover { background: #ccc; }
                        .open-link { color: #6ea8fe; }
                    }
                    .card {
                        text-align: center;
                        padding: 48px 40px;
                        max-width: 480px;
                        background: #fff;
                        border-radius: 12px;
                        box-shadow: 0 2px 8px rgba(0,0,0,0.08);
                    }
                    .icon {
                        width: 64px;
                        height: 64px;
                        margin: 0 auto 24px;
                        border-radius: 16px;
                        background: #e8e8e8;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        font-size: 28px;
                    }
                    h1 {
                        font-size: 18px;
                        font-weight: 600;
                        margin-bottom: 8px;
                    }
                    .error-detail {
                        font-size: 13px;
                        color: #888;
                        margin-bottom: 24px;
                        word-break: break-all;
                    }
                    .retry-btn {
                        display: inline-block;
                        padding: 10px 28px;
                        background: #333;
                        color: #fff;
                        border: none;
                        border-radius: 6px;
                        font-size: 14px;
                        cursor: pointer;
                        text-decoration: none;
                        margin-bottom: 16px;
                    }
                    .retry-btn:hover { background: #555; }
                    .open-link {
                        display: block;
                        font-size: 13px;
                        color: #4a90d9;
                        text-decoration: none;
                    }
                    .open-link:hover { text-decoration: underline; }
                </style>
            </head>
            <body>
                <div class="card">
                    <div class="icon">⚠</div>
                    <h1>$connectionFailed</h1>
                    <p class="error-detail">$escapedError (ERR_$errorCode)</p>
                    <a class="retry-btn" href="$escapedUrl">$retry</a>
                    <a class="open-link" href="uniso://open-external?url=${escapeHtml(failedUrl)}">$openInBrowser</a>
                </div>
            </body>
            </html>
            """.trimIndent()
    }

    private fun escapeHtml(text: String): String =
        text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
}
