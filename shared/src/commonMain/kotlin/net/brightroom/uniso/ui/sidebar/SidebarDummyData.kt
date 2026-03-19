package net.brightroom.uniso.ui.sidebar

import androidx.compose.ui.graphics.Color
import net.brightroom.uniso.ui.theme.ServiceColors

data class DummyAccount(
    val id: String,
    val serviceId: String,
    val serviceName: String,
    val accountName: String,
    val initials: String,
    val brandColor: Color,
    val url: String,
)

val dummyAccounts =
    listOf(
        DummyAccount("1", "x", "X", "@main_account", "MA", ServiceColors.X, "https://x.com/home"),
        DummyAccount("2", "x", "X", "@tech_updates", "TU", ServiceColors.X, "https://x.com/home"),
        DummyAccount("3", "instagram", "Instagram", "@my_photos", "MY", ServiceColors.Instagram, "https://www.instagram.com/"),
        DummyAccount("4", "instagram", "Instagram", "@biz_account", "BZ", ServiceColors.Instagram, "https://www.instagram.com/"),
        DummyAccount("5", "facebook", "Facebook", "Taro Yamada", "TY", ServiceColors.Facebook, "https://www.facebook.com/"),
        DummyAccount("6", "youtube", "YouTube", "My Channel", "MC", ServiceColors.YouTube, "https://www.youtube.com/"),
        DummyAccount("7", "bluesky", "Bluesky", "@user.bsky.social", "UB", ServiceColors.Bluesky, "https://bsky.app/"),
    )
