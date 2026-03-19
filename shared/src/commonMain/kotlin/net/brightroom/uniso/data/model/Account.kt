package net.brightroom.uniso.data.model

data class Account(
    val accountId: String,
    val serviceId: String,
    val displayName: String?,
    val avatarUrl: String?,
    val sortOrder: Int,
    val createdAt: String,
)
