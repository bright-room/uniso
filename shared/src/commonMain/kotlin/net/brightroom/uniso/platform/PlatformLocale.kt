package net.brightroom.uniso.platform

import net.brightroom.uniso.domain.settings.AppLocale

interface PlatformLocale {
    fun getSystemLocale(): AppLocale
}

expect fun loadStringResources(locale: AppLocale): Map<String, String>
