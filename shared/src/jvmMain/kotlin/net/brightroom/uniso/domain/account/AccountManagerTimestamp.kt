package net.brightroom.uniso.domain.account

import java.time.Instant

internal actual fun currentTimestamp(): String = Instant.now().toString()
