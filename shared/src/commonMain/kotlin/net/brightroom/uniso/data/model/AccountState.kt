package net.brightroom.uniso.data.model

data class AccountState(
    val accountId: String,
    val lastUrl: String?,
    val scrollPositionY: Int,
    val webviewStatus: String,
    val lastAccessedAt: String,
)
