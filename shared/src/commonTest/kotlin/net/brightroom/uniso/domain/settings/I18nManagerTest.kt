package net.brightroom.uniso.domain.settings

import net.brightroom.uniso.platform.PlatformLocale
import kotlin.test.Test
import kotlin.test.assertEquals

class I18nManagerTest {
    private fun createManager(
        systemLocale: AppLocale = AppLocale.EN,
        settingsRepository: SettingsRepository = InMemorySettingsRepository(),
    ): I18nManager {
        val platformLocale =
            object : PlatformLocale {
                override fun getSystemLocale(): AppLocale = systemLocale
            }
        return I18nManager(platformLocale, settingsRepository)
    }

    @Test
    fun initializeWithSystemLocaleJa() {
        val manager = createManager(systemLocale = AppLocale.JA)
        manager.initialize()

        assertEquals(AppLocale.JA, manager.currentLocale.value)
        assertEquals("アカウントを追加", manager.getString(StringKey.SIDEBAR_ADD_ACCOUNT))
    }

    @Test
    fun initializeWithSystemLocaleEn() {
        val manager = createManager(systemLocale = AppLocale.EN)
        manager.initialize()

        assertEquals(AppLocale.EN, manager.currentLocale.value)
        assertEquals("Add Account", manager.getString(StringKey.SIDEBAR_ADD_ACCOUNT))
    }

    @Test
    fun switchLocaleFromJaToEn() {
        val manager = createManager(systemLocale = AppLocale.JA)
        manager.initialize()

        manager.setLocale(AppLocale.EN)

        assertEquals(AppLocale.EN, manager.currentLocale.value)
        assertEquals("Add Account", manager.getString(StringKey.SIDEBAR_ADD_ACCOUNT))
        assertEquals("Settings", manager.getString(StringKey.SETTINGS_TITLE))
    }

    @Test
    fun switchLocaleFromEnToJa() {
        val manager = createManager(systemLocale = AppLocale.EN)
        manager.initialize()

        manager.setLocale(AppLocale.JA)

        assertEquals(AppLocale.JA, manager.currentLocale.value)
        assertEquals("アカウントを追加", manager.getString(StringKey.SIDEBAR_ADD_ACCOUNT))
        assertEquals("設定", manager.getString(StringKey.SETTINGS_TITLE))
    }

    @Test
    fun savedLocaleOverridesSystemLocale() {
        val settings = InMemorySettingsRepository()
        settings.setString("locale", "en")

        val manager = createManager(systemLocale = AppLocale.JA, settingsRepository = settings)
        manager.initialize()

        assertEquals(AppLocale.EN, manager.currentLocale.value)
        assertEquals("Add Account", manager.getString(StringKey.SIDEBAR_ADD_ACCOUNT))
    }

    @Test
    fun setLocalePersistsToSettings() {
        val settings = InMemorySettingsRepository()
        val manager = createManager(settingsRepository = settings)
        manager.initialize()

        manager.setLocale(AppLocale.JA)

        assertEquals("ja", settings.getString("locale"))
    }

    @Test
    fun getMissingKeyReturnsFallback() {
        val manager = createManager()
        // Before initialize, strings map is empty
        val result = manager.getString(StringKey.SIDEBAR_ADD_ACCOUNT)
        assertEquals(StringKey.SIDEBAR_ADD_ACCOUNT.key, result)
    }

    @Test
    fun allStringKeysHaveJaTranslation() {
        val manager = createManager(systemLocale = AppLocale.JA)
        manager.initialize()

        for (key in StringKey.entries) {
            val value = manager.getString(key)
            assert(value != key.key) {
                "Missing Japanese translation for key: ${key.key}"
            }
        }
    }

    @Test
    fun allStringKeysHaveEnTranslation() {
        val manager = createManager(systemLocale = AppLocale.EN)
        manager.initialize()

        for (key in StringKey.entries) {
            val value = manager.getString(key)
            assert(value != key.key) {
                "Missing English translation for key: ${key.key}"
            }
        }
    }
}
