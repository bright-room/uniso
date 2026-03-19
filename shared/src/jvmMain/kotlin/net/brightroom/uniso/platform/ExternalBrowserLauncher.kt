package net.brightroom.uniso.platform

import java.awt.Desktop
import java.net.URI

object ExternalBrowserLauncher {
    fun open(url: String) {
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(URI(url))
        }
    }
}
