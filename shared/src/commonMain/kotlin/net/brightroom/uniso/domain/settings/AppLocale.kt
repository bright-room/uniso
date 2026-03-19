package net.brightroom.uniso.domain.settings

enum class AppLocale(
    val code: String,
    val displayName: String,
) {
    JA("ja", "日本語"),
    EN("en", "English"),
    ;

    companion object {
        fun fromCode(code: String): AppLocale = entries.find { it.code == code } ?: EN
    }
}
