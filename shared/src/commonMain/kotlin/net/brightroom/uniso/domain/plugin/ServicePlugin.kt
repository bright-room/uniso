package net.brightroom.uniso.domain.plugin

data class ServicePlugin(
    val serviceId: String,
    val displayName: String,
    val domainPatterns: List<String>,
    val brandColor: Long,
    val iconResource: String,
    val authType: AuthType,
    val sortOrder: Int,
)

enum class AuthType {
    COOKIE,
    OAUTH,
    ;

    companion object {
        fun fromString(value: String): AuthType =
            when (value.lowercase()) {
                "oauth" -> OAUTH
                else -> COOKIE
            }
    }
}
