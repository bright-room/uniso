package net.brightroom.uniso.domain.settings

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import net.brightroom.uniso.platform.PlatformLocale
import net.brightroom.uniso.platform.loadStringResources

class I18nManager(
    private val platformLocale: PlatformLocale,
    private val settingsRepository: SettingsRepository,
) {
    private val _currentLocale = MutableStateFlow(AppLocale.EN)
    val currentLocale: StateFlow<AppLocale> = _currentLocale.asStateFlow()

    private var strings: Map<String, String> = emptyMap()

    fun initialize() {
        val savedLocaleCode = settingsRepository.getString(LOCALE_KEY)
        val locale =
            if (savedLocaleCode != null) {
                AppLocale.fromCode(savedLocaleCode)
            } else {
                platformLocale.getSystemLocale()
            }
        applyLocale(locale)
    }

    fun setLocale(locale: AppLocale) {
        settingsRepository.setString(LOCALE_KEY, locale.code)
        applyLocale(locale)
    }

    fun getString(key: StringKey): String = strings[key.key] ?: key.key

    private fun applyLocale(locale: AppLocale) {
        strings = loadStringResources(locale)
        _currentLocale.value = locale
    }

    companion object {
        private const val LOCALE_KEY = "locale"
    }
}
