package net.brightroom.uniso.domain.updater

data class UpdateInfo(
    val version: String,
    val releaseNotes: String,
    val downloadUrl: String,
)
