package net.brightroom.uniso.ui.webview

object UserAgentProvider {
    private val BASE_USER_AGENT =
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

    fun getUserAgent(serviceId: String): String =
        when (serviceId) {
            else -> BASE_USER_AGENT
        }
}
