package net.brightroom.uniso.ui.webview

import net.brightroom.uniso.domain.settings.SettingsRepository

object UserAgentProvider {
    private val DEFAULT_USER_AGENT =
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36"

    const val SETTINGS_KEY = "custom_user_agent"

    fun getUserAgent(settingsRepository: SettingsRepository? = null): String {
        val custom = settingsRepository?.getString(SETTINGS_KEY)
        return if (!custom.isNullOrBlank()) custom else DEFAULT_USER_AGENT
    }

    fun getDefaultUserAgent(): String = DEFAULT_USER_AGENT
}
