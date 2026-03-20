package net.brightroom.uniso.ui.webview

import net.brightroom.uniso.domain.settings.AppLocale
import net.brightroom.uniso.domain.settings.I18nManager
import net.brightroom.uniso.domain.settings.InMemorySettingsRepository
import net.brightroom.uniso.platform.PlatformLocale
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

class WebViewErrorPageTest {
    private lateinit var i18nManager: I18nManager

    @BeforeTest
    fun setup() {
        val platformLocale =
            object : PlatformLocale {
                override fun getSystemLocale(): AppLocale = AppLocale.EN
            }
        i18nManager = I18nManager(platformLocale, InMemorySettingsRepository())
        i18nManager.initialize()
    }

    @Test
    fun generates_html_with_error_info() {
        val html =
            WebViewErrorPage.generate(
                failedUrl = "https://x.com/home",
                errorCode = -105,
                errorText = "ERR_NAME_NOT_RESOLVED",
                i18nManager = i18nManager,
            )

        assertTrue(html.contains("Connection failed"), "Should contain localized error title")
        assertTrue(html.contains("Retry"), "Should contain retry button text")
        assertTrue(html.contains("Open in browser"), "Should contain open in browser link")
        assertTrue(html.contains("ERR_NAME_NOT_RESOLVED"), "Should contain error text")
        assertTrue(html.contains("-105"), "Should contain error code")
        assertTrue(html.contains("https://x.com/home"), "Should contain failed URL for retry")
    }

    @Test
    fun escapes_html_in_url() {
        val html =
            WebViewErrorPage.generate(
                failedUrl = "https://example.com/<script>alert(1)</script>",
                errorCode = -1,
                errorText = "test",
                i18nManager = i18nManager,
            )

        assertTrue(!html.contains("<script>"), "Should escape HTML tags in URL")
        assertTrue(html.contains("&lt;script&gt;"), "Should use HTML entities")
    }

    @Test
    fun generates_html_with_japanese_locale() {
        i18nManager.setLocale(AppLocale.JA)
        val html =
            WebViewErrorPage.generate(
                failedUrl = "https://x.com",
                errorCode = -105,
                errorText = "ERR_NAME_NOT_RESOLVED",
                i18nManager = i18nManager,
            )

        assertTrue(html.contains("接続に失敗しました"), "Should contain Japanese error title")
        assertTrue(html.contains("再試行"), "Should contain Japanese retry text")
    }
}
