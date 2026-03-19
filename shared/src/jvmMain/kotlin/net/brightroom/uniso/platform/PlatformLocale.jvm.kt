package net.brightroom.uniso.platform

import net.brightroom.uniso.domain.settings.AppLocale
import java.io.InputStreamReader
import java.util.Locale
import java.util.Properties

class JvmPlatformLocale : PlatformLocale {
    override fun getSystemLocale(): AppLocale {
        val language = Locale.getDefault().language
        return AppLocale.fromCode(language)
    }
}

actual fun loadStringResources(locale: AppLocale): Map<String, String> {
    val properties = Properties()
    val path = "i18n/strings_${locale.code}.properties"
    val stream =
        Thread.currentThread().contextClassLoader?.getResourceAsStream(path)
            ?: JvmPlatformLocale::class.java.classLoader?.getResourceAsStream(path)

    if (stream != null) {
        stream.use { inputStream ->
            InputStreamReader(inputStream, Charsets.UTF_8).use { reader ->
                properties.load(reader)
            }
        }
    }

    return properties.entries.associate { (key, value) ->
        key.toString() to value.toString()
    }
}
