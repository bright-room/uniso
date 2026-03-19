package net.brightroom.uniso.domain.session

data class SessionState(
    val activeAccountId: String?,
    val accountStates: List<AccountSessionState>,
    val wasCleanShutdown: Boolean,
)

data class AccountSessionState(
    val accountId: String,
    val lastUrl: String?,
    val scrollPositionY: Int,
    val webViewStatus: String,
)
