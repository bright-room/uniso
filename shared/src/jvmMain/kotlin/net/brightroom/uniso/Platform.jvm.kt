package net.brightroom.uniso

actual fun getPlatformName(): String {
    val os = System.getProperty("os.name")
    val arch = System.getProperty("os.arch")
    return "$os ($arch)"
}
