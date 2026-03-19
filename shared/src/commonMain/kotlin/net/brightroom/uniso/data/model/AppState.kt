package net.brightroom.uniso.data.model

data class AppState(
    val activeAccountId: String?,
    val cleanShutdown: Boolean,
    val lastSavedAt: String,
)
