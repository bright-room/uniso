package net.brightroom.uniso.ui.sidebar

import androidx.compose.ui.graphics.Color

data class SidebarAccount(
    val accountId: String,
    val serviceId: String,
    val serviceName: String,
    val accountName: String,
    val initials: String,
    val brandColor: Color,
    val url: String,
)
